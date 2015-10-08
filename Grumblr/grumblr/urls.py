from django.conf.urls import patterns, url, include
from grumblr import views
from grumblr.forms import *
from django.contrib import admin
from django.shortcuts import render, redirect
from django.contrib.auth.forms import PasswordChangeForm, SetPasswordForm

urlpatterns = patterns(
	"",
	url(r'^$', views.StartUp.as_view(), name='StartUp'),
	url(r'^home/', 'grumblr.views.home', name='home'),
	url(r'^add-post/', 'grumblr.views.AddPost', name='AddPost'), 
	url(r'^delete-post/(?P<id>\d+)$', 'grumblr.views.DeletePost', name='DeletePost'),
	url(r'^profile/','grumblr.views.Profile', name='Profile'), 
	url(r'^view_profile/', 'grumblr.views.ViewProfile', name='ViewProfile'),
	url(r'^logout$', 'django.contrib.auth.views.logout_then_login'),
	url(r'^register/', views.Register.as_view(), name='Register'),
	url(r'^login/', views.Login.as_view(), name='Login'), 
	url(r'^edit_profile/', 'grumblr.views.editProfile', name='editProfile'),
	url(r'^change_password/', 'django.contrib.auth.views.password_change', {'password_change_form':PasswordChangeForm, 'post_change_redirect':'/profile/'}, name='changePassword'),
	url(r'^photo/(?P<id>\d+)$', 'grumblr.views.getPhoto', name='getPhoto'),
	url(r'^follow_user/(?P<id>\d+)$', 'grumblr.views.FollowUser', name='FollowUser'),
	url(r'^unfollow_user/(?P<id>\d+)$', 'grumblr.views.UnFollowUser', name='UnFollowUser'),
	url(r'^followed_feeds/', 'grumblr.views.followedFeeds', name='followedFeeds'),
	url(r'^reset_password/', 'grumblr.views.resetPassword', name='resetPassword'),
	url(r'^reset_your_password/(?P<id>\d+)/$', 'grumblr.views.ResetYourPassword', name='ResetYourPassword'),
	url(r'^reset_password_for_user/(?P<id>\d+)/$', 'grumblr.views.ResetPasswordForUser', name='ResetPasswordForUser'),
    url(r'^admin/', include(admin.site.urls)),
	)