

CREATE INDEX IF NOT EXISTS idx_phone_change_requests_expires_at ON phone_change_requests (expires_at);
CREATE INDEX IF NOT EXISTS idx_phone_verification_codes_expires_at ON phone_verification_codes (expires_at);
CREATE INDEX IF NOT EXISTS idx_refresh_tokens_account_hashed_token ON refresh_tokens (account_id, hashed_token);
CREATE INDEX IF NOT EXISTS idx_refresh_tokens_expires_at ON refresh_tokens (expires_at);

