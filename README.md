# ModpackUpdater
A tool to update and install modpacks from a simple url, this tool allows you to include shaders and texture packs
[Official McMarket url](https://www.mc-market.org/resources/16787/)

## Creation
With this tool you can create modpacks and share them with your friends.
Now the tool doesn't allow you to create a .modpack file or something
like that to automatically open it and start installing the modpack,
but work is in progress and everything is possible

## How to?
This section will be divided in 2 parts, explanation and screenshots

### Explanation
First of all you should open ModpackTool.jar and wait until the tool
downloads ModpackUpdater.jar and opens it, after that, in ModpackUpdater
panel, click on "Creator panel", it will open a new panel, with a lot
of buttons and check boxes, the first 2 text boxes, are
1 - Modpack name
2 - Where the modpack info will be downloaded (needed)
for example, I want to create a modpack called "MCPack", and
I have a host which url is "https://mydomain.com/"
You should put that url in the 2nd text box

(If you don't have any forge version installed, creator panel will show an error, asking
you to install a forge version)

After configurin modpack info, you can go to the action.
Don't click "Create modpack" or "Open modpack files dir" button yet!
Check the boxes you want to:
Zip modpack - Will zip mods and versions folder in a zip (faster download)
Zip shaders - Will add shaderpacks folder to the zip
Zip texture packs - Will add resourcepacks folder to the zip
Perfom unzip debug - Unzip the modpack as it if had downloaded

Everything related to zip, will need "Zip modpack" checked to work, if you don't check zip modpack, 
resourcepacks and shaderpacks won't be added to the modpack

After checking boxes, you should pick up the forge version you want to use.
The nav under all these options, is a folder navigator, to choose where minecraft and its mods are installed,
if you installed .minecraft in another folder, or your mods are simply located on another folder, please use this
nav to navigate until you're inside the folder that contains mods folder, after that click "Open"

Once you've done everything previous, you can click "Create modpack"

### Screenshots
![open-tool](https://raw.githubusercontent.com/KarmaConfigs/ModpackUpdater/master/imgs/OpenTool.png)
![creator-panel](https://raw.githubusercontent.com/KarmaConfigs/ModpackUpdater/master/imgs/CreatorPanel.png)
![create-modpack](https://raw.githubusercontent.com/KarmaConfigs/ModpackUpdater/master/imgs/CreateModpack.png)
