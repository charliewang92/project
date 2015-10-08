# -*- coding: utf-8 -*-
from __future__ import unicode_literals

from django.db import models, migrations
import datetime
from django.utils.timezone import utc


class Migration(migrations.Migration):

    dependencies = [
        ('grumblr', '0008_auto_20151004_2348'),
    ]

    operations = [
        migrations.AddField(
            model_name='grumblruser',
            name='new_first_name',
            field=models.CharField(default=datetime.datetime(2015, 10, 5, 4, 23, 43, 940341, tzinfo=utc), max_length=5),
            preserve_default=False,
        ),
        migrations.AddField(
            model_name='grumblruser',
            name='new_last_name',
            field=models.CharField(default=datetime.datetime(2015, 10, 5, 4, 23, 55, 755211, tzinfo=utc), max_length=420),
            preserve_default=False,
        ),
    ]
