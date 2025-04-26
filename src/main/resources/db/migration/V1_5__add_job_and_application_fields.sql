-- Add created_at column to jobs table
ALTER TABLE jobs ADD COLUMN created_at DATETIME DEFAULT CURRENT_TIMESTAMP;

-- Add status column to jobs table if it doesn't exist
ALTER TABLE jobs ADD COLUMN status VARCHAR(20) DEFAULT 'OPEN';

-- Add applied_at column to job_applications table
ALTER TABLE job_applications ADD COLUMN applied_at DATETIME DEFAULT CURRENT_TIMESTAMP;

-- Update existing records
UPDATE jobs SET created_at = posting_date WHERE created_at IS NULL;
UPDATE job_applications SET applied_at = applied_date WHERE applied_at IS NULL; 
ALTER TABLE jobs ADD COLUMN created_at DATETIME DEFAULT CURRENT_TIMESTAMP;

-- Add status column to jobs table if it doesn't exist
ALTER TABLE jobs ADD COLUMN status VARCHAR(20) DEFAULT 'OPEN';

-- Add applied_at column to job_applications table
ALTER TABLE job_applications ADD COLUMN applied_at DATETIME DEFAULT CURRENT_TIMESTAMP;

-- Update existing records
UPDATE jobs SET created_at = posting_date WHERE created_at IS NULL;
UPDATE job_applications SET applied_at = applied_date WHERE applied_at IS NULL; 