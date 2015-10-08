# -*- coding: utf-8 -*-
from __future__ import unicode_literals

from django.db import models, migrations


class Migration(migrations.Migration):

    dependencies = [
        ('grumblr', '0003_auto_20150923_1800'),
    ]

    operations = [
        migrations.AddField(
            model_name='post',
            name='age',
            field=models.CharField(default='', max_length=5),
            preserve_default=False,
        ),
    ]
