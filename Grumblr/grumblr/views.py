import datetime
from django.shortcuts import render, redirect, get_object_or_404
from django.core.exceptions import ObjectDoesNotExist
from django.http import HttpResponse, Http404

#This decorator will make sure that the user is logged in before using this action
from django.contrib.auth.decorators import login_required

#Models that will reflect the user's post and the user currently logged in
from django.contrib.auth.models import User
from django.contrib.auth import update_session_auth_hash
from django.contrib.auth.forms import PasswordChangeForm
from grumblr.models import *
from grumblr.forms import *
#Using the djagno authentication module to facilitate loging 
from django.contrib.auth import login, authenticate
from django.views.generic import TemplateView
from django.core.mail import send_mail
import itertools
from mimetypes import guess_type
from django.contrib.auth.tokens import default_token_generator
from django.contrib.auth.hashers import make_password


# Create your views here.

# Create your views here.
class StartUp(TemplateView):
	context={}
	context['register_form'] = RegistrationForm()
	context['login_form'] = LoginForm()
	context['reset_email_form'] = resetPasswordForm()
	def get(self, request):
		response = render(request, 'grumblr/index.html', self.context)
		return response

	def post(self, request):
		response = render(request, 'grumblr/index.html', self.context)
		return response

@login_required
def home(request):
	context = {}
	user = request.user
	posts = Post.objects.all()	
	posts_list = list(posts)
	posts_list.sort(key=lambda x: x.post_date)
	posts_list.reverse()
	posts = posts_list
	if not user:
		return render(request, 'grumblr/index.html')
	context['user'] = user
	context['posts'] = posts
	context['add_entry_form'] = PostForm()
	response = render(request, 'grumblr/logged_in.html', context)
	return response

@login_required
def AddPost(request):
	context = {}
	errors = []
	if request.method == 'GET':
		return redirect('/home/')
	
	if request.method == 'POST':
		new_post = Post(user=request.user)
		form = PostForm(request.POST, instance=new_post)
		if not form.is_valid():
			context['entryForm'] = form
			return render(request, 'grumblr/logged_in.html', context)
		else:
			new_post.setDate(datetime.datetime.now())
			form.save()
			return redirect('/home/')


@login_required
def DeletePost(request, id):
	return redirect('/home/')

@login_required
def Profile(request):
	errors = []
	context = {}
	context['errors'] = errors
	context['edit_user_form'] = GrumblrUserForm()
	context['change_password_form'] = PasswordChangeForm(request)
	if request.method == 'GET' or request.method == 'POST':
		user = User.objects.get(username=request.user)
		g_user = GrumblrUser.objects.get(user=user)
		if not user:
			return redict('/')
		posts = Post.objects.filter(user_id=user)
		posts_list = list(posts)
		posts_list.sort(key=lambda x: x.post_date)
		posts_list.reverse()
		posts = posts_list
		context['user'] = user
		context['posts'] = posts
		context['age'] = g_user.age
		context['bio'] = g_user.bio
		return render(request, 'grumblr/profile_page.html', context)
			
@login_required
def ViewProfile(request):
	errors = []
	context = {}
	context['errors'] = errors

	if request.method == 'GET':
		return redirect('/home/')

	if request.method == 'POST':
		form = viewProfileForm(request.POST)
		context['form'] = form
		if not form.is_valid():
			return render(request, 'grumblr/logged_in.html', context)
		
		user_lookup = User.objects.get(username=request.POST['username'])
		posts = Post.objects.filter(user_id=user_lookup.id)
		posts_list = list(posts)
		posts_list.sort(key=lambda x: x.post_date)
		posts_list.reverse()
		posts = posts_list
		context['user'] = user_lookup
		context['posts'] = posts
		context['just_viewing'] = True
		context['original_user'] = request.user
		context['follow_form'] = UsersFollowingOthersForm()
		return render(request, 'grumblr/profile_page.html', context)

class Login(TemplateView):
	def get(self, request):
		context={}
		context['register_form'] = RegistrationForm()
		context['login_form'] = LoginForm()
		context['reset_email_form'] = resetPasswordForm()
		response = render(request, 'grumblr/index.html', context)
		return response

	def post(self, request):
		errors = []
		context = {'errors': errors}
		context['register_form'] = RegistrationForm()
		context['login_form'] = LoginForm()
		context['reset_email_form'] = resetPasswordForm()
		form = LoginForm(request.POST)
		context['form'] = form 
		if not form.is_valid():
			return render(request, 'grumblr/index.html', context)

		logging_user = authenticate(username=request.POST['username'], \
									password=request.POST['password'])
		login(request, logging_user)
		return redirect('/home/')



class Register(TemplateView):
	def get(self, request):
		context = {}
		context['register_form'] = RegistrationForm()
		context['login_form'] = LoginForm()
		response = render(request, 'grumblr/index.html', context)
		return response

	def post(self, request):
		errors = []
		context = {'errors': errors}
		context['register_form'] = RegistrationForm()
		context['login_form'] = LoginForm()
		form = RegistrationForm(request.POST)
		context['form'] = form

		if not form.is_valid():
			return render(request, 'grumblr/index.html', context)

		#There were no errors, now we will create the user with all the valid fields
		new_user = User.objects.create_user(first_name=request.POST['first_name'], \
											last_name=request.POST['last_name'], \
											email=request.POST['email'], \
											username=request.POST['username'], \
											password=request.POST['password'])
		new_grumblr_user = GrumblrUser(user=new_user)
		new_users_following_others = UsersFollowingOthers(user=new_user)
		new_user.save()
		new_grumblr_user.save()
		new_users_following_others.save()
		new_user = authenticate(username=request.POST['username'], \
								password=request.POST['password'])
		login(request, new_user)
		return redirect('/home/')

@login_required
def editProfile(request):
	context = {}
	if request.method == 'GET':
		return redirect('/profile/')
	
	if request.method == 'POST':
		form = GrumblrUserForm(request.POST)
		if not form.is_valid():
			return redirect('/profile/')
		grumblr_user = GrumblrUser.objects.get(user=request.user)

		if request.POST['new_first_name']:
			request.user.first_name = request.POST['new_first_name']
		if request.POST['new_last_name']:
			request.user.last_name = request.POST['new_last_name']
		if request.POST['age']:
			grumblr_user.age = request.POST['age']
		if request.POST['bio']:
			grumblr_user.bio = request.POST['bio']
		if 'picture' in request.FILES:
			grumblr_user.picture = request.FILES['picture']


		grumblr_user.save()
		request.user.save()
		return redirect('/profile/')

@login_required
def getPhoto(request, id):
	user = User.objects.filter(id=id)
	if len(user) <= 0:
		return HttpResponse("no user", content_type='html/txt')
	grumblr_user = GrumblrUser.objects.get(user=user)
	if not grumblr_user.picture:
		return HttpResponse('no picture!', content_type='text/html')
	if not grumblr_user:
		raise Http404
	# content_type = guess_type(grumblr_user.picture.name)
	return HttpResponse(grumblr_user.picture, content_type='image/*')

@login_required
def FollowUser(request, id):
	if request.method == "GET":
		return redirect('/profile/')

	if request.method == "POST":
		user_followed = User.objects.filter(id=id)
		if len(user_followed) <= 0:
			return redirect('/view_profile/')
		user_followed = user_followed[0]
		grumblr_user_followed = GrumblrUser.objects.get(user=user_followed)
		userFollowing = UsersFollowingOthers.objects.get(user=request.user)
		userFollowing.FollowedUsers.add(grumblr_user_followed)
		userFollowing.save()
		context = {}
		context['sucess'] = 'Successfully followed!'
		return redirect('/view_profile/')

@login_required
def UnFollowUser(request, id):
	if request.method == 'GET':
		return redirect('/profile/')
	if request.method == 'POST':
		user_unfollowed = User.objects.filter(id=id)
		if len(user_unfollowed) <= 0:
			return redirect('/view_profile/')
		user_unfollowed = user_unfollowed[0]
		grumblr_user_unfollowed = GrumblrUser.objects.get(user=user_unfollowed)
		userUnFollowing = UsersFollowingOthers.objects.get(user=request.user)
		userUnFollowing.FollowedUsers.remove(grumblr_user_unfollowed)
		userUnFollowing.save()
		return redirect('/view_profile/')

@login_required
def followedFeeds(request):
	context = {}
	if request.method == 'GET' or request.method == 'POST':
		user = request.user
		user_following = UsersFollowingOthers.objects.get(user=user)
		followed_people = user_following.FollowedUsers.all()
		posts_list = []
		for followed_user in followed_people:
			val = Post.objects.filter(user=followed_user.user)
			posts_list.append(val)
		finished_list = list(posts_list)
		merged = list(itertools.chain(*finished_list))
		merged.sort(key=lambda x: x.post_date)
		merged2 = list(reversed(merged))
		context['user'] = user
		context['posts'] = merged2
		return render(request, 'grumblr/followed_users.html', context)

def resetPassword(request):
	context = {}
	context['login_form'] = LoginForm()
	context['register_form'] = RegistrationForm()
	context['reset_email_form'] = resetPasswordForm()
	if request.method =='GET':
		return redirect('/')

	if request.method == 'POST':
		reset_email_form = resetPasswordForm(request.POST) 
		if not reset_email_form.is_valid():
			context['login_form'] = LoginForm()
			context['register_form'] = RegistrationForm()
			context['reset_email_form'] = reset_email_form
			return render(request, 'grumblr/index.html', context)
		else:
			user = User.objects.get(username=request.POST['username'])
			token = default_token_generator.make_token(user)
			user.token = token
			user.save()
			email_body="This email is being sent to you because you are requesting a password reset for username %s.\
						If this is not you, please ignore this message. If it is, please click on the link and follow the \
						directions there: localhost:8000/reset_your_password/%s/" % (user.username, user.id)
			send_mail(subject="Reset your password for grumblr", 
					  message=email_body, 	
					  from_email="support_Do_Not_Reply@grumblr.com",
					  recipient_list=[request.POST['email']])
			context['email_sent'] = "Reset email has been sent!"
			return render(request, 'grumblr/index.html', context)

def ResetYourPassword(request, id):
	context = {}
	if request.method == 'GET':
		context['form'] = resetForm()
		user = User.objects.filter(id=id)
		if len(user) <= 0:
			return redirect('/')
		user = user[0]
		context['user'] = user
		return render(request, 'grumblr/password_reset.html', context)

	if request.method == 'POST':
		return redirect('/')

def ResetPasswordForUser(request, id):
	context = {}
	if request.method == 'GET':
		return redirect('/')

	if request.method == 'POST':
		form = resetForm(request.POST)
		if not form.is_valid():
			context['register_form'] = RegistrationForm()
			context['login_form'] = LoginForm()
			context['reset_email_form'] = resetPasswordForm()
			return render(request, 'grumblr/index.html', context)

		else:
			user = User.objects.get(id=id)
			user.password = make_password(request.POST['password'])
			user.save()
			context['register_form'] = RegistrationForm()
			context['login_form'] = LoginForm()
			context['reset_email_form'] = resetPasswordForm()
			context['sucessful_edit'] = 'You have successfully changed your password!'
			return render(request, 'grumblr/index.html', context)


 
















