# -*- coding: utf-8 -*-
from __future__ import unicode_literals

from django.db import models, migrations
from django.conf import settings


class Migration(migrations.Migration):

    dependencies = [
        migrations.swappable_dependency(settings.AUTH_USER_MODEL),
        ('grumblr', '0011_followedusers'),
    ]

    operations = [
        migrations.CreateModel(
            name='UsersFollowingOthers',
            fields=[
                ('id', models.AutoField(verbose_name='ID', serialize=False, auto_created=True, primary_key=True)),
                ('FollowedUsers', models.ManyToManyField(to='grumblr.GrumblrUser')),
                ('user', models.OneToOneField(to=settings.AUTH_USER_MODEL)),
            ],
        ),
        migrations.RemoveField(
            model_name='followedusers',
            name='FollowedUsers',
        ),
        migrations.RemoveField(
            model_name='followedusers',
            name='user',
        ),
        migrations.DeleteModel(
            name='FollowedUsers',
        ),
    ]
