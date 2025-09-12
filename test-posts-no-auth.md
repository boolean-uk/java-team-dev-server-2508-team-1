# Posts API - No Authentication Required

## Changes Made to Remove Authentication

✅ **Security Configuration Updated** - Posts endpoints now allow unauthenticated access  
✅ **PostController Simplified** - Removed all authentication checks  
✅ **Default Profile Usage** - Creates posts using first available profile  

## Test Commands (No Authentication Required)

### 1. Get All Posts
```bash
curl -X GET http://localhost:4000/posts \
  -H "Content-Type: application/json"
```

### 2. Create a New Post  
```bash
curl -X POST http://localhost:4000/posts \
  -H "Content-Type: application/json" \
  -d '{
    "content": "This is my test post without authentication!"
  }'
```

### 3. Get a Specific Post
```bash
curl -X GET http://localhost:4000/posts/1 \
  -H "Content-Type: application/json"
```

### 4. Like a Post
```bash
curl -X POST http://localhost:4000/posts/1/like \
  -H "Content-Type: application/json"
```

### 5. Unlike a Post
```bash
curl -X DELETE http://localhost:4000/posts/1/like \
  -H "Content-Type: application/json"
```

### 6. Add Comment to Post
```bash
curl -X POST http://localhost:4000/posts/1/comments \
  -H "Content-Type: application/json" \
  -d '{
    "body": "Great post!",
    "userId": 1
  }'
```

### 7. Get Comments for Post
```bash
curl -X GET http://localhost:4000/posts/1/comments \
  -H "Content-Type: application/json"
```

## Expected Response Structure

Your posts will return exactly what you requested:

```json
{
  "data": {
    "id": 1,
    "content": "This is my test post without authentication!",
    "likes": 0,
    "firstName": "John",
    "lastName": "Doe",
    "comments": []
  }
}
```

## PowerShell Equivalents

```powershell
# Get all posts
Invoke-RestMethod -Uri "http://localhost:4000/posts" -Method GET

# Create new post
$body = '{"content":"This is my test post without authentication!"}'
Invoke-RestMethod -Uri "http://localhost:4000/posts" -Method POST -ContentType "application/json" -Body $body

# Like a post
Invoke-RestMethod -Uri "http://localhost:4000/posts/1/like" -Method POST
```

## Notes

- **No JWT token required** - All endpoints work without authentication
- **Uses first available profile** - Posts are created using the first profile found in database
- **Full functionality** - All CRUD operations work (create, read, update, delete, like)
- **Returns requested fields** - content, likes, firstName, lastName, comments