import { createClient } from '@supabase/supabase-js'

// In dev, falls back to local Supabase. In production, VITE_ env vars must be set.
const supabaseUrl = import.meta.env.VITE_SUPABASE_URL || 'http://127.0.0.1:54321'
const supabaseAnonKey = import.meta.env.VITE_SUPABASE_ANON_KEY || (
  import.meta.env.DEV ? 'sb_publishable_ACJWlzQHlZjBrEguHvfOxg_3BJgxAaH' : ''
)

export const supabase = createClient(supabaseUrl, supabaseAnonKey)
