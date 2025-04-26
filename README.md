# OpnHire - Job Search Platform

A complete job search platform connecting job seekers with recruiters.

## Deployment Guide for Render

### Prerequisites
- GitHub account with repository access
- Render account
- AWS account (for S3 file storage)

### Step 1: Database Setup
The application uses PostgreSQL database on Render:

- Database Name: opnhire_db
- Username: opnhire_db_user
- Region: Singapore

### Step 2: AWS S3 Setup
1. Create an S3 bucket named "opnhire-uploads"
2. Create an IAM user with S3 access permissions
3. Get access key and secret key for the IAM user
4. Apply appropriate bucket policy for security

### Step 3: Deploy Web Service on Render
1. Create a new Web Service in Render
2. Link your GitHub repository
3. Configure the service:
   - Name: opnhire
   - Environment: Java
   - Build Command: `./mvnw clean package -DskipTests`
   - Start Command: `java -jar target/UserBackend-0.0.1-SNAPSHOT.jar --spring.profiles.active=prod`
   - Instance Type: Free

4. Add Environment Variables:
   ```
   SPRING_PROFILES_ACTIVE=prod
   
   # Database config
   JDBC_DATABASE_URL=[Your database URL from Render dashboard]
   JDBC_DATABASE_USERNAME=[Your database username]
   JDBC_DATABASE_PASSWORD=[Your database password]
   
   # AWS S3 config
   AWS_ACCESS_KEY_ID=[Your AWS access key]
   AWS_SECRET_KEY=[Your AWS secret key]
   AWS_REGION=us-east-1
   AWS_S3_BUCKET=opnhire-uploads
   
   # Email config
   MAIL_USERNAME=[Your email address]
   MAIL_PASSWORD=[Your email password/app password]
   
   # JWT config
   JWT_SECRET=[Your secure random string for JWT]
   ```

5. Click "Create Web Service"

### Step 4: Verify Deployment
1. Wait for build and deployment to complete
2. Access your application at the URL provided by Render
3. Check logs for any errors or issues
4. Test login, file uploads, and other functionality

## Local Development

### Prerequisites
- Java 17
- Maven
- MySQL (development) / PostgreSQL (production)

### Running Locally
1. Clone the repository
2. Configure database in application.properties
3. Run `./mvnw spring-boot:run`
4. Access application at http://localhost:8081

## Features
- User authentication and registration
- Job seeker and recruiter profiles
- Job posting and application
- Chat system for communication
- File attachments for resumes and documents 