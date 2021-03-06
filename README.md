<p align="center"><img src="https://github.com/Louismousine/imagify/blob/master/logo.JPG"/></p>

Imagify is my submission for the [Shopify Backend Challenge for Summer 2021](https://docs.google.com/document/d/1ZKRywXQLZWOqVOHC4JkF3LqdpO3Llpfk_CkZPR8bjak/edit). It is an image repository written in Spring Boot, and it has the following features:
* Create an account
* Login
* Upload an image
* Get images that are similar to the requested image (similarity is based on the [Amazon Rekognition API](https://docs.aws.amazon.com/rekognition/latest/dg/what-is.html))
* Edit the image's visibility to private / public
* Edit the tags of an image
* Search images by tags
* Get images by ID, user or all images
* Like / unlike an image

where all of the operations aside from register and login require a JWT token to authenticate the user and validate the request. More information about each of these features can be found under the Endpoint subsection. For convenience, the images are stored and served in an AWS bucket. This allows using the AWS Rekognition API to detect labels in images to assess similarity.

## Using the application

The application is hosted at https://image-repo-shopify.herokuapp.com/. To test the endpoints, use [this Postman collection](https://github.com/Louismousine/imagify/blob/master/Imagify.postman_collection.json) in Postman to send the requests. 

IMPORTANT NOTE: Since it is hosted on Heroku, it takes the application a few seconds to start up after receiving the first request. It will respond quickly after that!

## Response bodies
In the endpoints section, I will refer to two specific bodies that can be returned. They have the following structures (with example values provided):
1. UserDTO \
`{
    "username": "User",
    "password": null,
    "images": [],
    "likedImages": [],
    "apiToken": "eyJhbGciOiJIUzI1NiJ9.eyJzdWIiOiJVc2VyIiwiaWF0IjoxNjA5ODE1NzQ1LCJleHAiOjE2MDk4MTkzNDV9.Gv-zOjVahoyLKYO-uXuUTFIEYRfxryOfLSopGb0SaCo"
}`
2. ImageDTO \
`{
    "id": 2,
    "picture": "https://s3.us-east-2.amazonaws.com/shopifybackendimage/1609816154507-75553019_2662993437121434_3389314969953632256_o(1).jpg",
    "ownerUsername": "User",
    "tags": [
        "louis"
    ],
    "labels": [
    	"Text"
    ],
    "numberOfLikes": 0,
    "private": false
}`
## Endpoints

### User

* `api/user/register` - POST - Creates a user account with the specified password and username. The password is encrypted before being stored in the database. Example JSON: `{
	"password":"UserPW",
	"username":"User"
}`. The response is a UserDTO which holds the API token to be used in subsequent requests. If using the Postman collection, store this token in the token variable in the Imagify collection.

* `api/user/login` - POST - Logs in an existing account to retrieve the JWT token. Example JSON: `{
	"password":"UserPW",
	"username":"User"
}`. The response is a UserDTO which holds the API token to be used in subsequent requests. If using the Postman collection, store this token in the token variable in the Imagify collection. 

### Image

For the remaining endpoints, the JWT token is always required as a header for authentication purposes. One has to log in/register to get this token. Also, the return type is always an ImageDTO (or a list of ImageDTOs where applicable) except for the delete endpoint.

* `api/image/upload?tags={}&isPrivate={}` - POST - Uploads an image that is then stored in the AWS bucket. The tags parameter in the URL is a string that holds words from which the image can be looked up in a search. For multiple tags, separate the tags by commas as follows "shopify,image,repository". The isPrivate query parameter is a boolean that determines if the image will be accessible by other users if false or if it will only be accessible to the owner if true. This endpoint also takes in an image of type JPG or PNG (However, note that in some cases the Rekognition API rejects the images if they are too large or of an unsupported type in which case the image will not have labels by which to be searched). In Postman, one can select "form-data" under body, add a key "imageFile" with the image file as a value. The response is an ImageDTO which holds the link to the stored picture.

* `api/image/{id}` - GET - Accesses an image by its id. Returns an ImageDTO if the image exists and the requester is either the owner of the picture or the picture is public. Returns a 400 error otherwise.

* `api/image/findSimilar/{id}` - GET - Gets a list of images that have similar keywords as determined by the Rekognition API.

* `api/image/` - GET - Gets the list of all images that are accessible by the requester - both public images and private images uploaded by the requester.

* `api/image/user/{username}` - GET - Gets the list of all images that were uploaded by the User with username {username} and are accessible to the requester.

* `api/image/find?search=tags:*{tag}*` - GET - Operates as the search feature by getting all images that have the tag {tag} and are accessible to the requester.

* `api/image?ids={}` - DELETE - Deletes the images with the specified IDs. One can delete multiple images by separating IDs with commas. The images are only deleted if the owner makes the request.

* `api/image/tags/{id}?tags={tags}` - PUT - Updates the image with ID {id}'s tags to {tags} where {tags} is a string of comma separated words if the requester is the image's owner.

* `api/image/private/{id}` - PUT - Updates the image with ID {id}'s visibility to private if the image was public or vice versa and if the requester is the image's owner.

* `api/image/like/{id}` - POST - Makes the requester add a Like or remove a Like from image with ID {id}.
