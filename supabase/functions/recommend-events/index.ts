import { serve } from "https://deno.land/std@0.168.0/http/server.ts";
import { createClient } from "https://esm.sh/@supabase/supabase-js@2";

const corsHeaders = {
  "Access-Control-Allow-Origin": "*",
  "Access-Control-Allow-Headers":
    "authorization, x-client-info, apikey, content-type",
};

serve(async (req) => {
  if (req.method === "OPTIONS") {
    return new Response("ok", { headers: corsHeaders });
  }

  try {
    const { userId } = await req.json();

    if (!userId) {
      return new Response(JSON.stringify({ error: "userId is required" }), {
        status: 400,
        headers: { ...corsHeaders, "Content-Type": "application/json" },
      });
    }

    // Service role key bypasses RLS so we can read all data server-side
    const supabase = createClient(
      Deno.env.get("SUPABASE_URL")!,
      Deno.env.get("SUPABASE_SERVICE_ROLE_KEY")!
    );

    // Fetch user's interests
    const { data: profileInterests } = await supabase
      .from("profile_interests")
      .select("interests(name)")
      .eq("profile_id", userId);

    const userInterests =
      profileInterests?.map((pi: any) => pi.interests?.name).filter(Boolean) ?? [];

    // Fetch user's past joined events for behavioural matching
    const { data: pastParticipations } = await supabase
      .from("event_participants")
      .select("events(title, description)")
      .eq("profile_id", userId)
      .limit(10);

    const pastEventTitles =
      pastParticipations?.map((p: any) => p.events?.title).filter(Boolean) ?? [];

    // Fetch upcoming public events
    const { data: events } = await supabase
      .from("events")
      .select(
        "id, title, description, location_name, lat, lng, date_time, max_participants, is_public, key_interests, creator_id, group_id, created_at"
      )
      .eq("is_public", true)
      .gte("date_time", new Date().toISOString())
      .order("date_time", { ascending: true })
      .limit(50);

    if (!events || events.length === 0) {
      return new Response(JSON.stringify([]), {
        headers: { ...corsHeaders, "Content-Type": "application/json" },
      });
    }

    // Build prompt for Claude
    const eventsText = events
      .map(
        (e: any, i: number) =>
          `${i + 1}. [id:${e.id}] "${e.title}"${e.description ? ` — ${e.description}` : ""} at ${e.location_name}`
      )
      .join("\n");

    const prompt = `You are a recommendation engine for Wandr, a social travel app.

User interests: ${userInterests.length > 0 ? userInterests.join(", ") : "none set"}
Past events attended: ${pastEventTitles.length > 0 ? pastEventTitles.join(", ") : "none"}

Upcoming events to rank:
${eventsText}

Rank these events by how relevant they are to this user based on their interests and past activity.
Consider semantic relationships — for example "hiking" relates to someone interested in "Climbing" or "Nature".

Return ONLY a valid JSON array sorted by score descending. No markdown, no explanation, no extra text.
Format: [{"eventId":"<id>","score":<0.0-1.0>}]`;

    // Call Claude API
    const claudeResponse = await fetch("https://api.anthropic.com/v1/messages", {
      method: "POST",
      headers: {
        "Content-Type": "application/json",
        "x-api-key": Deno.env.get("ANTHROPIC_API_KEY")!,
        "anthropic-version": "2023-06-01",
      },
      body: JSON.stringify({
        model: "claude-haiku-4-5-20251001",
        max_tokens: 1024,
        messages: [{ role: "user", content: prompt }],
      }),
    });

    const claudeData = await claudeResponse.json();
    const rankingText = claudeData.content[0].text.trim();

    // Sort events by Claude's scores. Fall back to chronological if parsing fails.
    let rankedEvents = events;
    try {
      const rankings: Array<{ eventId: string; score: number }> = JSON.parse(rankingText);
      const scoreMap = new Map(rankings.map((r) => [r.eventId, r.score]));
      rankedEvents = [...events].sort(
        (a: any, b: any) => (scoreMap.get(b.id) ?? 0) - (scoreMap.get(a.id) ?? 0)
      );
    } catch {
      // Claude returned unexpected format — keep chronological order as fallback
    }

    return new Response(JSON.stringify(rankedEvents), {
      headers: { ...corsHeaders, "Content-Type": "application/json" },
    });
  } catch (error: any) {
    return new Response(JSON.stringify({ error: error.message }), {
      status: 500,
      headers: { ...corsHeaders, "Content-Type": "application/json" },
    });
  }
});
