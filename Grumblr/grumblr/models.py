import datetime
from django.db import models
# Create your models here.
from django.contrib.auth.models import User
from django.utils import timezone 

class Post(models.Model):
	post_content = models.CharField(max_length=42)
	user = models.ForeignKey(User)
	post_date = models.DateTimeField(default=timezone.now, blank=True) 

	def __unicode__(self):
		return self.post_content

	def getUser(self):
		return self.user

	def getTimePosted(self):
		return self.post_date

	def setDate(self, date):
		self.post_date = date

	@staticmethod
	def get_posts(owner):
		return Entry.objects.filter(owner=owner).order_by('post_date')

class GrumblrUser(models.Model):
	user = models.OneToOneField(User)
	age = models.CharField(max_length=5)
	bio = models.CharField(max_length=420)
	new_first_name = models.CharField(max_length=420)
	new_last_name = models.CharField(max_length=420)
	picture = models.ImageField(upload_to='profile_photos', blank=True)

	def __unicode__(self):
		return self.age

class UsersFollowingOthers(models.Model):
	user = models.OneToOneField(User)
	FollowedUsers = models.ManyToManyField(GrumblrUser)

	def __unicode__(self):
		return self.user.username
