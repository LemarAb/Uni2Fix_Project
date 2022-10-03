from django.shortcuts import render
from rest_framework import viewsets, permissions, authentication
from .serializers import ProblemSerializer, UserSerializer
from .models import MockReport, User
from rest_framework.decorators import api_view
from rest_framework.response import Response
#from django.contrib.auth.models import User
import json

# Create your views here.
class ProblemViewSet(viewsets.ModelViewSet):
    queryset = MockReport.objects.all().order_by('id')
    serializer_class = ProblemSerializer
    permission_classes = [permissions.IsAuthenticated]
    authentication_classes = [authentication.TokenAuthentication]

class UserViewSet(viewsets.ModelViewSet):
    queryset = User.objects.all().order_by('id')
    serializer_class = UserSerializer
    permission_classes = [permissions.IsAuthenticated]
    authentication_classes = [authentication.TokenAuthentication]

@api_view(["POST"])
def api_auth(request, *args, **kwargs):
    '''
    Authenticates the user for accessing the database
    '''
    username = request.data["username"]
    password = request.data["password"]
    print(request.data)

    user = authentication.authenticate(username=username, password=password)
    
    if user is not None:
        return Response({"result": "Success"})

    return Response({"result": "Failed"})

@api_view(["POST"])
def api_register(request, *args, **kwargs):
    '''
    Registers a new user
    '''
    print(request.body)
    username = request.data["username"]
    password = request.data["password"]

    if str(username).strip() == "":
        return Response({"result": "Failed"})

    new_user = User()
    try:
        new_user = User.objects.get(username=username)
    except User.DoesNotExist:
        new_user.username = username
        new_user.set_password(password)
        new_user.save()
        new_user = authentication.authenticate(username=username, password=password)
        if new_user is not None:
            return Response({"result": "Success", "id": new_user.id, "isAdmin": new_user.isAdmin, "email": new_user.email, "username": new_user.username}) #todo: send the user information as well
        return Response({"result": "Failed"})


    '''
    new_user, user_created = User.objects.get_or_create(username=username, password=password)
    if user_created:
        new_user.set_password(password)
        new_user.save()
        #return Response({"result": "Success"})
    '''
    print({"result": "Success", "id": new_user.id, "isAdmin": new_user.isAdmin, "email": new_user.email, "username": new_user.username})
    return Response({"result": "Success", "id": new_user.id, "isAdmin": new_user.isAdmin, "email": new_user.email, "username": new_user.username})
    #return Response({"result": "Failed"})
