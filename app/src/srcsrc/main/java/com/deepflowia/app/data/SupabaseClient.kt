package com.deepflowia.app.data

import io.supabase.gotrue.GoTrue
import io.supabase.gotrue.GoTrueDefault
import io.supabase.gotrue.gotrue
import io.supabase.postgrest.Postgrest
import io.supabase.postgrest.postgrest
import io.supabase.realtime.Realtime
import io.supabase.realtime.realtime
import io.supabase.storage.Storage
import io.supabase.storage.storage

object SupabaseClient {
    private const val supabaseUrl = "https://xzgdfetnjnwrberyddmf.supabase.co"
    private const val supabaseKey = "eyJhbGciOiJIUzI1NiIsInR5cCI6IkpXVCJ9.eyJpc3MiOiJzdXBhYmFzZSIsInJlZiI6Inh6Z2RmZXRuam53cmJlcnlkZG1mIiwicm9sZSI6ImFub24iLCJpYXQiOjE3NDIzMjk4MTksImV4cCI6MjA1NzkwNTgxOX0.XJFYvBiZHo1vcfCV6Fn79C9U6LP4Vuf05PCixBWqaYU"

    val client = io.supabase.SupabaseClient(
        supabaseUrl = supabaseUrl,
        supabaseKey = supabaseKey
    ) {
        install(GoTrue)
        install(Postgrest)
        install(Realtime)
        install(Storage)
    }
}