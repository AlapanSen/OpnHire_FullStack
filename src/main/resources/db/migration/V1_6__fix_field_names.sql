-- Ensure the applied_at column exists in job_applications table
ALTER TABLE job_applications ADD COLUMN IF NOT EXISTS applied_at DATETIME DEFAULT CURRENT_TIMESTAMP;

-- Update all records to have applied_at matching their appliedDate if it's NULL
UPDATE job_applications SET applied_at = applied_date WHERE applied_at IS NULL;

-- Ensure the created_at column exists in jobs table
ALTER TABLE jobs ADD COLUMN IF NOT EXISTS created_at DATETIME DEFAULT CURRENT_TIMESTAMP;

-- Update all records to have created_at matching their postingDate if it's NULL
UPDATE jobs SET created_at = posting_date WHERE created_at IS NULL;

-- Make sure the status column exists in the jobs table
ALTER TABLE jobs ADD COLUMN IF NOT EXISTS status VARCHAR(20) DEFAULT 'OPEN'; 
ALTER TABLE job_applications ADD COLUMN IF NOT EXISTS applied_at DATETIME DEFAULT CURRENT_TIMESTAMP;

-- Update all records to have applied_at matching their appliedDate if it's NULL
UPDATE job_applications SET applied_at = applied_date WHERE applied_at IS NULL;

-- Ensure the created_at column exists in jobs table
ALTER TABLE jobs ADD COLUMN IF NOT EXISTS created_at DATETIME DEFAULT CURRENT_TIMESTAMP;

-- Update all records to have created_at matching their postingDate if it's NULL
UPDATE jobs SET created_at = posting_date WHERE created_at IS NULL;

-- Make sure the status column exists in the jobs table
ALTER TABLE jobs ADD COLUMN IF NOT EXISTS status VARCHAR(20) DEFAULT 'OPEN'; 