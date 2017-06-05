@echo off
cd ../..
echo -------------Message.jar---------------------------
cd ./Message
call ant -buildfile ./build_no_api.xml default
if not exist ../jar/PlatformMessage.jar (
	msg compile error
)

echo -------------CorgiServerCore.jar---------------------------
cd ..
cd ./CorgiServerCore
call ant -buildfile ./build_no_api.xml default
if not exist ../jar/CorgiServerCore.jar (
	msg compile error
)

echo -------------ManageServer.jar---------------------------
cd ..
cd ./ManageServer
call ant -buildfile ./build_no_api.xml default
if not exist ../jar/ManageServer.jar (
	msg compile error
)

echo -------------InterfaceServer.jar---------------------------
cd ..
cd ./InterfaceServer
call ant -buildfile ./build_no_api.xml default
if not exist ../jar/InterfaceServer.jar (
	msg compile error
)

echo -------------AuthServer.jar---------------------------
cd ..
cd ./AuthServer
call ant -buildfile ./build_no_api.xml default
if not exist ../jar/AuthServer.jar (
	msg compile error
)

echo -------------GameServer.jar---------------------------
cd ..
cd ./GameServer
call ant -buildfile ./build_no_api.xml default
if not exist ../jar/GameServer.jar (
	msg compile error
)

echo -------------ClubServer.jar---------------------------
cd ..
cd ./ClubServer
call ant -buildfile ./build_no_api.xml default
if not exist ../jar/ClubServer.jar (
	msg compile error
)

echo -------------BattlePlatform.jar---------------------------
cd ..
cd ./BattlePlatform
call ant -buildfile ./build_no_api.xml default
if not exist ../jar/BattlePlatform.jar (
	msg compile error
)

cd ..\..
echo -----Success !!!-----------
pause