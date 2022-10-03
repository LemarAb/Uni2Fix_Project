from django.contrib import admin
from .models import MockReport, User

# Register your models here.
admin.site.register([MockReport, User])