# -*- coding: utf-8 -*-
from __future__ import unicode_literals

from django.db import models, migrations


class Migration(migrations.Migration):

    dependencies = [
        ('grumblr', '0009_auto_20151005_0023'),
    ]

    operations = [
        migrations.AddField(
            model_name='grumblruser',
            name='picture',
            field=models.ImageField(upload_to=b'profile_photos', blank=True),
        ),
    ]
