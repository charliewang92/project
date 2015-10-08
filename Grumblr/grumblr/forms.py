import datetime
from django import forms
from django.contrib.auth import login, authenticate

from django.contrib.auth.models import User
from models import *

class PostForm(forms.ModelForm):
	class Meta:
		model = Post
		exclude = ('user', 'post_date',)

	def clean(self):
		cleaned_data = super(PostForm, self).clean()
		post_content = cleaned_data.get('post_content')
		if not post_content:
			raise forms.ValidationError('Please enter some content first!')


class GrumblrUserForm(forms.ModelForm):
	new_first_name = forms.CharField(max_length=420,
								label='First Name',
								widget=forms.TextInput(attrs={'class':'form-control'}),
								required=False)
	new_last_name = forms.CharField(max_length=420,
								label='Last Name',
								widget=forms.TextInput(attrs={'class':'form-control'}),
								required=False)
	age = forms.CharField(max_length=420,
						  label='Age',
						  widget=forms.TextInput(attrs={'class':'form-control'}),
						  required=False)
	bio = forms.CharField(max_length=420,
								label='Biography',
								widget=forms.Textarea(attrs={'class':'form-control'}),
								required=False)
	picture = forms.CharField(max_length=420,
								label='Picture',
								widget=forms.FileInput(),
								required=False)

	class Meta:
		model = GrumblrUser
		exclude = ('user',)
		widget = {'picture': forms.FileInput()}


class UsersFollowingOthersForm(forms.ModelForm):
	class Meta:
		model = UsersFollowingOthers
		exclude = ('user',)

class RegistrationForm(forms.Form):
	username = forms.CharField(max_length=30,
								label='Username',
								widget=forms.TextInput(attrs={'class':'form-control'}))
	first_name = forms.CharField(max_length=30,
								label='First Name',
								widget=forms.TextInput(attrs={'class':'form-control'}))
	last_name = forms.CharField(max_length=30, 
								label='Last Name',
								widget=forms.TextInput(attrs={'class':'form-control'}))
	email = forms.CharField(max_length=100,
								help_text='Valid email addresses only!', 
								label='Email',
							   widget=forms.TextInput(attrs={'class':'form-control'}))
	password = forms.CharField(max_length=200,
								label='Password',
								widget=forms.PasswordInput(attrs={'class':'form-control'}))
	confirm_password = forms.CharField(max_length=200,
									   label='Confirm Password',
									   widget=forms.PasswordInput(attrs={'class':'form-control'}))
	def clean(self):
		cleaned_data = super(RegistrationForm, self).clean()
		password = cleaned_data.get('password')
		confirm_password = cleaned_data.get('confirm_password')
		email = cleaned_data.get('email')
		first_name = cleaned_data.get('first_name')
		last_name = cleaned_data.get('last_name')
		if password and confirm_password and password != confirm_password:
			raise forms.ValidationError("Passwords did not match!")

		username = self.cleaned_data.get('username')
		if User.objects.filter(username__exact=username):
			raise forms.ValidationError("Username is already taken!")

		if not first_name or not last_name or not email:
			raise forms.ValidationError('Please make sure to enter First Name, Last Name, and Email for registration')
		return cleaned_data		

class LoginForm(forms.Form):
	username = forms.CharField(max_length=30, 
							   label='Username',
							   widget=forms.TextInput(attrs={'class':'form-control'}))
	password = forms.CharField(max_length=30,
							   label='Password',
							   widget=forms.PasswordInput(attrs={'class':'form-control'}))

	def clean(self):
		cleaned_data = super(LoginForm, self).clean()

		username = cleaned_data.get('username')
		password = cleaned_data.get('password')

		if not username:
			raise forms.ValidationError('Please Enter a Username!')

		if not password:
			raise forms.ValidationError('Please Enter a Password!')

		logging_user_list = User.objects.filter(username=username)
		if not logging_user_list:
			raise forms.ValidationError('Your username does not exist in our records!')

		logging_user = authenticate(username=username, \
									password=password)
		if not logging_user:
			raise forms.ValidationError("Your username or password is incorrect! Please try again")

		return cleaned_data	

class viewProfileForm(forms.Form):
	username = forms.CharField(max_length=30)

	def clean(self):
		cleaned_data = super(viewProfileForm, self).clean()

		username=cleaned_data.get('username')
		if not username:
			raise forms.ValidationError('This user does not exist!')
		else:
			try:
				user_lookup = User.objects.get(username=username)
			except:
				raise forms.ValidationError('User you were looking for (%s) was not found!' % username) 

class resetPasswordForm(forms.Form):
	username = forms.CharField(max_length=30,
								label='Username',
								widget=forms.TextInput(attrs={'class':'form-control'}))
	email = forms.CharField(max_length=100,
								help_text='Valid email addresses only!', 
								label='Email',
							   widget=forms.TextInput(attrs={'class':'form-control'}))
	def clean(self):
		cleaned_data = super(resetPasswordForm, self).clean()

		username = cleaned_data.get('username')
		email = cleaned_data.get('email')

		if not username or not email:
			raise forms.ValidationError('Please enter a username and email address!')

		user = User.objects.filter(username=username)
		user = user[0]
		if not user:
			raise forms.ValidationError('This user does not exist!')

		if user.email != email:
			raise forms.ValidationError('The username you entered does not match the email that we have on record!')

		return cleaned_data

class resetForm(forms.Form):
	password = forms.CharField(max_length=200,
								label='Password',
								widget=forms.PasswordInput(attrs={'class':'form-control'}))

	confirm_password = forms.CharField(max_length=200,
								label='Confirm Password',
								widget=forms.PasswordInput(attrs={'class':'form-control'}))

	def clean(self):
		cleaned_data = super(resetForm, self).clean()
		password = cleaned_data.get('password')
		confirm_password = cleaned_data.get('confirm_password')
		if password and confirm_password and password != confirm_password:
			raise forms.ValidationError("Passwords did not match!")

		return cleaned_data	































