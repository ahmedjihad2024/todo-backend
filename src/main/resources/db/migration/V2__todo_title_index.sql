
-- title INDEX
CREATE EXTENSION IF NOT EXISTS pg_trgm;
CREATE INDEX IF NOT EXISTS idx_todos_title ON todos USING GIN(LOWER(title) gin_trgm_ops);

-- accountId and createdAt INDEX
CREATE INDEX IF NOT EXISTS idx_todos_account_created ON todos (account_id, created_at);

-- accountId and completed INDEX
CREATE INDEX IF NOT EXISTS idx_todos_account_id_completed ON todos (account_id, completed);

-- accountId and priority INDEX
CREATE INDEX IF NOT EXISTS idx_todos_account_id_priority ON todos (account_id, priority);

-- accountId and dueDate INDEX
CREATE INDEX IF NOT EXISTS idx_todos_account_id_due_date ON todos (account_id, due_date);