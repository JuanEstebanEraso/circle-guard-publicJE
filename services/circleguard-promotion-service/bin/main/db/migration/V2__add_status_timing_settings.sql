CREATE TABLE IF NOT EXISTS system_settings (
    id UUID PRIMARY KEY DEFAULT gen_random_uuid(),
    created_at TIMESTAMP WITH TIME ZONE DEFAULT CURRENT_TIMESTAMP
);

ALTER TABLE system_settings 
ADD COLUMN IF NOT EXISTS mandatory_fence_days INTEGER NOT NULL DEFAULT 14,
ADD COLUMN IF NOT EXISTS encounter_window_days INTEGER NOT NULL DEFAULT 14;

-- Seed initial values if not present
UPDATE system_settings SET mandatory_fence_days = 14, encounter_window_days = 14 WHERE mandatory_fence_days IS NULL;
