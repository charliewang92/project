# -*- coding: utf-8 -*-
from __future__ import unicode_literals

from django.db import models, migrations


class Migration(migrations.Migration):

    dependencies = [
        ('grumblr', '0004_post_age'),
    ]

    operations = [
        migrations.RemoveField(
            model_name='post',
            name='age',
        ),
    ]
