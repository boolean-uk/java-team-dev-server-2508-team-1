# Posts API Testing Guide

## What Your Posts API Now Returns

Your posts endpoints now return exactly what you requested:
- **content** (post body/text)
- **likes** (number of likes)
- **firstName** (from linked profile)
- **lastName** (from linked profile)  
- **comments** (array of comments on the post)

## Example Response Structure

### GET /posts Response
```json
{
  "data": [
    {
      "id": 1,
      "content": "This is my first post!",
      "likes": 5,
      "firstName": "John",
      "lastName": "Doe",
      "comments": [
        {
          "id": 1,
          "body": "Great post!",
          "user": {
            "id": 2,
            "email": "jane@example.com"
          }
        },
        {
          "id": 2, 
          "body": "Thanks for sharing!",
          "user": {
            "id": 3,
            "email": "bob@example.com"
          }
        }
      ]
    }
  ]
}
```

### POST /posts Response
```json
{
  "data": {
    "id": 2,
    "content": "My new post content",
    "likes": 0,
    "firstName": "John",
    "lastName": "Doe", 
    "comments": []
  }
}
```

## Test Commands

### 1. Login to get JWT token
```bash
curl -X POST http://localhost:4000/login \
  -H "Content-Type: application/json" \
  -d '{
    "email": "your-email@example.com",
    "password": "your-password"
  }'
```

### 2. Get all posts
```bash
curl -X GET http://localhost:4000/posts \
  -H "Authorization: Bearer YOUR_JWT_TOKEN" \
  -H "Content-Type: application/json"
```

### 3. Create a new post
```bash
curl -X POST http://localhost:4000/posts \
  -H "Authorization: Bearer YOUR_JWT_TOKEN" \
  -H "Content-Type: application/json" \
  -d '{
    "content": "This is my test post content!"
  }'
```

### 4. Like a post
```bash
curl -X POST http://localhost:4000/posts/1/like \
  -H "Authorization: Bearer YOUR_JWT_TOKEN" \
  -H "Content-Type: application/json"
```

### 5. Get a specific post
```bash
curl -X GET http://localhost:4000/posts/1 \
  -H "Authorization: Bearer YOUR_JWT_TOKEN" \
  -H "Content-Type: application/json"
```

## Key Features

✅ **Authentication Required**: All endpoints require valid JWT token  
✅ **Profile Integration**: firstName/lastName come from user's profile  
✅ **Comments Included**: All comments are loaded with each post  
✅ **Like System**: Posts can be liked/unliked  
✅ **Clean Response**: Only returns the fields you requested  

## Database Structure

- Posts table has `profile_id` foreign key (not `user_id`)
- Profile contains `first_name` and `last_name` 
- Comments are linked to posts via `post_id`
- Likes are stored as integer count on each post