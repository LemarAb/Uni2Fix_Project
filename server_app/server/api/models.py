from django.db import models
from django.contrib.auth.models import User
from django.db.models.signals import post_save
from django.dispatch import receiver
from django.contrib.auth.models import AbstractUser

class MockReport(models.Model):
    reportTitle = models.CharField(max_length=200)
    reportDate = models.CharField(max_length=200)
    reportBuildingRoom = models.CharField(max_length=200)
    reportText = models.CharField(max_length=200)
    reportStatus = models.IntegerField()
    reportLikes = models.IntegerField()
    reportDislikes = models.IntegerField()
    reportImg = models.CharField(max_length=10000000000000)


class User(AbstractUser):
    isAdmin = models.BooleanField(default=False, null=True)


'''
class Profile(models.Model):
    user = models.OneToOneField(User, on_delete=models.CASCADE)
    isAdmin = models.BooleanField()

    @receiver(post_save, sender=User)
    def create_user_profile(sender, instance, created, **kwargs):
        if created:
            Profile.objects.create(user=instance)

    @receiver(post_save, sender=User)
    def save_user_profile(sender, instance, **kwargs):
        instance.profile.save()
        '''