-- Add jsonb preference column and accumulators
ALTER TABLE user_preferences
    ADD COLUMN IF NOT EXISTS preference_json jsonb;

ALTER TABLE user_preferences
    ADD COLUMN IF NOT EXISTS preference_accum jsonb;

ALTER TABLE user_preferences
    ADD COLUMN IF NOT EXISTS weight_sum double precision DEFAULT 0;

-- Optional: create table for fast feature lookup if not using element collection
CREATE TABLE IF NOT EXISTS user_preference_vectors (
    id serial PRIMARY KEY,
    user_preference_id bigint NOT NULL REFERENCES user_preferences(id) ON DELETE CASCADE,
    feature varchar(255) NOT NULL,
    weight double precision NOT NULL,
    UNIQUE(user_preference_id, feature)
);

-- Optional: install pgvector and add column (requires superuser)
-- CREATE EXTENSION IF NOT EXISTS vector;
-- ALTER TABLE user_preferences ADD COLUMN IF NOT EXISTS preference_vector vector(30);

-- Indexes to speed reads
CREATE INDEX IF NOT EXISTS idx_user_preferences_userid ON user_preferences(user_id);
CREATE INDEX IF NOT EXISTS idx_user_preference_vectors_prefid ON user_preference_vectors(user_preference_id);
