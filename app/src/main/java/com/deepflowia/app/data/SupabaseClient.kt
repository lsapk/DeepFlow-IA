package com.deepflowia.app.data

import com.deepflowia.app.BuildConfig
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
    val client = io.supabase.SupabaseClient(
        supabaseUrl = BuildConfig.SUPABASE_URL,
        supabaseKey = BuildConfig.SUPABASE_ANON_KEY
    ) {
        install(GoTrue)
        install(Postgrest)
        install(Realtime)
        install(Storage)
    }
}