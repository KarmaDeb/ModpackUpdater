# ModpackUpdater
A tool to update and install modpacks from a simple url, this tool allows you to include shaders and texture packs
[Official McMarket url](https://www.mc-market.org/resources/16787/)

## Creation
With this tool you can create modpacks and share them with your friends.
Now the tool doesn't allow you to create a .modpack file or something
like that to automatically open it and start installing the modpack,
but work is in progress and everything is possible

## How to?
When you open the tool, this UI will apear, clik on create<br>
to open creator UI<br>
![creator.png](https://raw.githubusercontent.com/KarmaConfigs/ModpackUpdater/master/imgs/create.png)<br>
<br>
After clicking on it, you will se something like this<br>
![creator_pane_1.png](https://raw.githubusercontent.com/KarmaConfigs/ModpackUpdater/master/imgs/creator_pane_1.png)<br>
<br>
Fill the info starting from the modpack name, here goes<br>
your modpack name, it can contain special characters<br>
![creator_pane_name.png](https://raw.githubusercontent.com/KarmaConfigs/ModpackUpdater/master/imgs/creator_pane_name.png)<br>
<br>
After doing that, you must sepecify the authors of the modpack<br>
You should put the mod authors of the modpack that requires you to<br>
leave credits if you use his mod on a modpack, and yours<br>
![creator_pane_author.png](https://raw.githubusercontent.com/KarmaConfigs/ModpackUpdater/master/imgs/creator_pane_author.png)<br>
<br>
After setting authors, you must tell the creator what's the version of the modpack<br>
***THIS IS NOT MINECRAFT VERSION***<br>
![creator_pane_version.png](https://raw.githubusercontent.com/KarmaConfigs/ModpackUpdater/master/imgs/creator_pane_version.png)<br>
<br>
Then you must set the modpack description, here you should put<br>
a little bit of what the modpack is orientated on and credits for<br>
the mods that requires it, or a link to your community<br>
![creator_pane_description.png](https://raw.githubusercontent.com/KarmaConfigs/ModpackUpdater/master/imgs/creator_pane_description.png)<br>
<br>
And finally, select the forge/liteloader version and configure the<br> 
modpack options using the options pane on the right<br>
![creator_option_forge.png](https://raw.githubusercontent.com/KarmaConfigs/ModpackUpdater/master/imgs/creator_option_forge.png)<br>
![creator_option_misc.png](https://raw.githubusercontent.com/KarmaConfigs/ModpackUpdater/master/imgs/creator_option_misc.png)<br>
<br>
And as a final step, make sure the url is pointing to where you will upload<br>
modpack generated files and click create<br>
![creator_pane_2.png](https://raw.githubusercontent.com/KarmaConfigs/ModpackUpdater/master/imgs/creator_pane_2.png)<br>
<br>
After making sure the tool coppied all the mods and versions<br>
locate roaming folder and open the folder MPU/modpacks<br>
after that, open the folder with the name of your modpack and open it<br>
Finally, open the upload folder and copy all the files inside of it<br>
to your host ( If you specified the host url as https://myhost.com/modpacks/mymodpack )<br>
you must upload these files into ftp://myhost.com/modpacks/mymodpack<br>
![final_step.png](https://raw.githubusercontent.com/KarmaConfigs/ModpackUpdater/master/imgs/final_step.png)
<br>
## Wait, I don't have a host
This tool requires you a host to download the files<br>
remotely<br>
<br>
<br>
## Nice, but... how to download it?
It's the most easy thing to do, if you uploaded the modpack to a host<br>
put that host url into *Download url* input<br>
![download_1.png](https://raw.githubusercontent.com/KarmaConfigs/ModpackUpdater/master/imgs/download_1.png)<br>
<br>
You will see that red border, that's because your url isn't pointing<br>
to the .mpu file you should be uploaded to your host, fix it by<br>
adding "/<ModpackFileName>.mpu"<br>
![download_final.png](https://raw.githubusercontent.com/KarmaConfigs/ModpackUpdater/master/imgs/download_final.png)<br>
Finally click on "Download" and enjoy!<br>
<br>
## But hey, my host url is too long and it looks bad!
Don't worry, you can share the .mpu file with your friends/community<br>
and tell them to open it using the "Open" button<br>
in the tool<br>
![download_locally_final.png](https://raw.githubusercontent.com/KarmaConfigs/ModpackUpdater/master/imgs/download_locally_final.png)<br>
And say them to click download button after it<br>
<br>the tool should read from the local .mpu file<br>
<br>
<br>
### Yay, you're done!
