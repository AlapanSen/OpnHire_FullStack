-- Create chats table
CREATE TABLE chats (
    id BIGINT AUTO_INCREMENT PRIMARY KEY,
    name VARCHAR(255),
    is_group BOOLEAN DEFAULT FALSE,
    created_at TIMESTAMP,
    updated_at TIMESTAMP
);

-- Create chat_participants table (many-to-many relation)
CREATE TABLE chat_participants (
    chat_id BIGINT NOT NULL,
    user_id BIGINT NOT NULL,
    PRIMARY KEY (chat_id, user_id),
    FOREIGN KEY (chat_id) REFERENCES chats (id) ON DELETE CASCADE,
    FOREIGN KEY (user_id) REFERENCES users (id) ON DELETE CASCADE
);

-- Create messages table
CREATE TABLE messages (
    id BIGINT AUTO_INCREMENT PRIMARY KEY,
    chat_id BIGINT NOT NULL,
    sender_id BIGINT NOT NULL,
    content TEXT,
    attachment_url VARCHAR(255),
    attachment_type VARCHAR(100),
    attachment_name VARCHAR(255),
    sent_at TIMESTAMP NOT NULL,
    read_at TIMESTAMP,
    status VARCHAR(20) DEFAULT 'SENT',
    FOREIGN KEY (chat_id) REFERENCES chats (id) ON DELETE CASCADE,
    FOREIGN KEY (sender_id) REFERENCES users (id) ON DELETE CASCADE
);

-- Create indexes for better performance
CREATE INDEX idx_messages_chat_id ON messages (chat_id);
CREATE INDEX idx_messages_sender_id ON messages (sender_id);
CREATE INDEX idx_messages_sent_at ON messages (sent_at);
CREATE INDEX idx_chats_updated_at ON chats (updated_at); 
CREATE TABLE chats (
    id BIGINT AUTO_INCREMENT PRIMARY KEY,
    name VARCHAR(255),
    is_group BOOLEAN DEFAULT FALSE,
    created_at TIMESTAMP,
    updated_at TIMESTAMP
);

-- Create chat_participants table (many-to-many relation)
CREATE TABLE chat_participants (
    chat_id BIGINT NOT NULL,
    user_id BIGINT NOT NULL,
    PRIMARY KEY (chat_id, user_id),
    FOREIGN KEY (chat_id) REFERENCES chats (id) ON DELETE CASCADE,
    FOREIGN KEY (user_id) REFERENCES users (id) ON DELETE CASCADE
);

-- Create messages table
CREATE TABLE messages (
    id BIGINT AUTO_INCREMENT PRIMARY KEY,
    chat_id BIGINT NOT NULL,
    sender_id BIGINT NOT NULL,
    content TEXT,
    attachment_url VARCHAR(255),
    attachment_type VARCHAR(100),
    attachment_name VARCHAR(255),
    sent_at TIMESTAMP NOT NULL,
    read_at TIMESTAMP,
    status VARCHAR(20) DEFAULT 'SENT',
    FOREIGN KEY (chat_id) REFERENCES chats (id) ON DELETE CASCADE,
    FOREIGN KEY (sender_id) REFERENCES users (id) ON DELETE CASCADE
);

-- Create indexes for better performance
CREATE INDEX idx_messages_chat_id ON messages (chat_id);
CREATE INDEX idx_messages_sender_id ON messages (sender_id);
CREATE INDEX idx_messages_sent_at ON messages (sent_at);
CREATE INDEX idx_chats_updated_at ON chats (updated_at); 