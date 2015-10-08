To turn in homework 4, create files (and subdirectories if needed) in
this directory, add and commit those files to your cloned repository,
and push your commit to your bare repository on GitHub.

Add any general notes or instructions for the TAs to this README file.
The TAs will read this file before evaluating your work.

Disclaimer: I apologize for the long list of details, I wanted to be as thorough as possible and state my assumptions, etc. 

Refactoring of work: 
	- I primarily did my refactoring using template inheritance of the large structures in the HTML. I created two base templates, one for the login form which will serve the login page and the password reset page and the other "base_interface.html" which will server as the base for the pages that work with users who have logged in. I chose this approach as I saw the logged in pages had more similarities with each other and it did not make sense to just extend everything from one base file. 

	- The main things I refactored are the headers for each page, where the user's own profiles are displayed and how each post is displayed. I moved all of this out onto the base template so that I only had to customize specific features such as the profile picture in the profile page. 

	- For forms, I refactored the add-post, login, and register forms to use model forms for the add-post and regular django forms for registering and login. 

	- While the forms allowed for much fewer checks in the views.py file, it did introduce the hassle of having to create a form and passing the form with the context to the front end with each of my render calls. As now if I were to redirect the user to a different page that needed the previous form, it is not in the template.html anymore but rather rendered inside of the context['form']

	- Finally, form validation was moved all to the forms.py file. However, I left some of the data-logic such as checking if a user exists in the database out in the views.py file. This was a decision I made because it separated out the input validation portion from the data validation portion. 

Features break down: 

Allowing users to edit information:
	- To extend the User model given with django I created a grumblruser with a onetoonefield with the user model
	- This allowed me to add on profile data such as ln/fn, age and bio fields without having to tamper too much with the User model
	- I also extended the grumblruser to be a model form so that when a user is created in the registration step a grumblruser is created with it as well. Keeping the onetoone relationship. 
	- Some checks here were the ability to update individual fields instead of all the fields.

Edit password: 
	- I wanted to practice using django's password changing built-in tool
	- Instead of creating a form with this, I used django's PasswordChangeForm
	- While this made the implementation easier, and allowing me to skip having to authenticate the user and restoring their session, it introduced an assumption. 
	- Assumption: When user typing in their old password, they must know what their old password is. If they do not know what their old password is or type it in incorrectly, the application will send the user to the django admin console's html, as it is linked by default. 
	- The django admin console's page is just another html page that the user can change their passwords with their old and new passwords. 
	- It is not actually linked to the "admin" of the console, I am just borrowing django's page.
	- Once the password is changed from this page, the user will be taken back to their profile page 
	- I chose to leave this behavior as it still allows the user to chagne their password and keep them authenticated afterwards, fulfilling the requirement

Upload/view image:
	- First time users do not have an image. I display the alt message "Get a pic" in lieu of their message until they upload one. 
	- The user may upload an image using the upload feature as part of the update user profile button
	- Assumption: Each user only has 1 image allocated to them, if they upload a new image, the old one will be deleted as each image is associated with their grumblruser profile.
	- For serving the image files, In each of the posts, upper righthand corner and profile page pic request by ID and I pull up the grumblruser's id and server their picture. 
	- Pictures are stored in the /media folder inside of the grumblr folder
	- I pip-installed pillow in order for this to work so you may have to pip-install pillow as well

Follow/Unfollow users:
	-Similar to the grumblruser, I had to create a new model to fit the users that will be following each other.
	-I could not find a quick way to just extend grumblruser as this required a manytomany relationship and I already had a onetoone relationship with each user. 
	- The UsersFollowingOthers model has a onetoone with each user and a manytomany with each grumblr user. 
	- I also created a model form for this and each time a user is created, a UsersFollowingOthers is created, preserving the onetoone relationship. 
	- Each time a user follows a target, I add to their list of targets, this new target. Django provides uniqueness check for me so if they follow a user twice, it will only be followed once. 
	- Each time a user unfollows a target, the target is removed from the followed list. 
	- Assumption: I allowed the user to follow themselves as I believe users have to right to follow their own feed if they would like to. This was not in the spec so I decided to implement it in this way. 
	- Assumption: I use reverse URL + id of the target to request to the server for follow/unfollow, if somehow the user tries to follow a user with id not in the database, I send the user back to the profile that they were viewing, and ignore the request. 

Follower Stream: 
	- This is just a new page of all the user's UsersFollowingOthers's followed user's list.
	- For each one I grab the user and I flatten the list of users to a single list
	- Sort the list by date
	- Reverse the list so that the most recent goes on top

New users must register with their e-mail address:
	- Carried over from previous homework as this was there before

Users can reset their password by link:
	- Emails are sent via the terminal due to lack of mail server backend
	- The emails are sent with the same message each time, but the link set is localhost:8000/reset_your_password/user_id/
	- The new page I created will render the user's ID and use it to pull the user's id
	- I played around with the token that django offered and was getting bugs. 
	- Because using a token was not part of the requirement or specifications, I only idenfitied the user with the email and ID pairing. 
	- Assumption: Users will only access this portion of the password reset via email, as they will not know the actual user_id stored in the database. 
	- I understand that exposing the ID in this way is a huge security flaw. However, for the purpose of allowing a user reset their password without any additional implementation, this was the quickest way to bootstrap a system in place while still fulfilling the specifications
	- New passwords are set via a hashing algorithm that will be recognized by the authentication system. 
	- I chose to not authenticate the user after they have reset the password. Instead, I send them back to the initial login page and have them login again. 


Additional notes: 
- Some forms were not updated to use django forms because they were simple one line forms to get to a user's profile
Since they did not demonstrate the new features (form input validation because they used django's reverse url feature)  that django forms were taking advantages of, I chose not to update all the forms, only the ones that directly related to model forms, and large forms such as registering users and adding/following users/posts respectively. 
- Pages validate with exception django template and django forms introduce errors. 


Use Case:
If get requests are sent to forms expecting post data, they will be redirected to a reasonable location
Users may get to their profile on the upper left hand corner by clicking their picture or name
Users may follow themselves if they click on a picture of their own post to go to the "view_profile" mode and follow/unfollow there
The upper left hand corner grumblr logo will always take the user back to the main page with the stream of all posts from all users or if they are logged out, to the sign in page. 



Suggested Homework turnin structure:

[YOUR-ANDREW-ID]/homework/4/
	webapps/
		settings.py
		urls.py
	grumblr/
		static/
			grumblr/
		templates/	
			grumblr/
		models.py
		views.py
	manage.py
	db.sqlite3
	README.md


My File Structure: 

charliew/homework/4/
	webapps/
		urls.py
		settings.py
		[etc..]
	grumblr
		media/ #may be created after file uploads
		migrations/
		static/
			grumblr/
				css/
					cover.css
					etc...
				js/
					bootstrap.js
					etc...
		templates/
			grumblr/
				base.html...
				etc...
		admin.py
		forms.py
		models.py
		tests.py
		etc...
	manage.py
	README.md

Please note the discrepancies in folder structure. 


Bibliography:
1.W3C school for html/css guidelines
2.http://www.cleancss.com/html-beautify/ 
3.https://docs.djangoproject.com/en/1.8/topics/auth/default/#session-invalidation-on-password-change
4.Stackoverflow for various python/html language syntax and errors/parametrs
5. Bootstrap templates: cover and dashboard 



