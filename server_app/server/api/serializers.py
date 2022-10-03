from rest_framework import serializers
from .models import MockReport, User

class ProblemSerializer(serializers.HyperlinkedModelSerializer):
    class Meta:
        model = MockReport
        fields = ('id', 'reportTitle', 'reportDate', 'reportBuildingRoom', 
                 'reportText', 'reportStatus', 'reportLikes', 'reportDislikes', 'reportImg')

class UserSerializer(serializers.ModelSerializer):
    class Meta:
        model = User
        fields = ('id', 'isAdmin', 'username', 'email')