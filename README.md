# CC-1.18.2 Converter

The CC-1.18.2-Converter is a Java-based tool designed to convert Minecraft worlds from version 1.12.2 to version 1.18.2, specifically for use with creative building in BTE worlds. It allows users to convert the world with specific height settings and use a paper 1.18.2 server with the Converter plugin for further conversion of special blocks. The program uses a command-line interface and allows you to choose the number of threads used in the conversion process. The resulting output includes converted regions, post-processing and entity locations if needed.

### Entities
Entities will be converted but only the supported ones:
- Armor Stand 
- Boat 
- Chest Minecart 
- Command Block Minecart 
- Ender Crystal 
- Furnace Minecart 
- Hopper Minecart 
- Item 
- Item Frame 
- Minecart 
- Painting 
- Shulker 
- TNT 
- TNT Minecart 
- Wither Skull

Commands in the Command Block Minecart are converted as well, but only the commands `blockdata`, `entitydata`, and `execute`. Other commands are transferred over as they are. <br>The full list of supported items are: Golden Apple, Boat, Reeds, Fish, Cooked Fish, Dye, Melon, Speckled Melon, Spawn Egg, Firework Charge, Fireworks, Netherbrick, Banner (Patters as well), Mob Spawner, Writable Book, Knowledge Book, Skull (Custom skulls as well), Chorus Fruit Popped, Filled Maps and Music Disc's.

#### Filled Maps
Filled Maps (item and placed map) are supported as well, but it's only been tested on Paper, so it might not work on other Minecraft servers. The `maps` folder in the output folder can contain one or more `maps_[session]` folders, with the `session` being a random ID which is generated each time the converter is run. This approach enables the conversion of multiple worlds to a single converted world, as it avoids duplicate map item ID's.<br> This approach also avoids writing the full map item data (each pixel color for example) for each occurrence of the filled map placed in the world or in the inventory.

## Tested versions

[![521](https://img.shields.io/badge/Paper-1.19.4%20%23521-green)](https://api.papermc.io/v2/projects/paper/versions/1.19.4/builds/521/downloads/paper-1.19.4-521.jar)
[![388](https://img.shields.io/badge/Paper-1.18.2%20%23388-green)](https://api.papermc.io/v2/projects/paper/versions/1.18.2/builds/388/downloads/paper-1.18.2-388.jar)


## Instructions for Using the Program
- Make sure that your offsets are in values of 16.
- Determine the number of logical processors available in your system. You can find this information in Task Manager under the Performance tab. The program will not allow you to exceed the available amount of logical processors.
- Ensure that the minimum Y Value is set to the minimum Y Value of your 1.18.2 world. By default, this is -64, but with the datapack, it can be up to -2032.
- Ensure that the maximum Y Value is set to the maximum Y Value of your 1.18.2 world. By default, this is 320, but with the datapack, it can be up to 2016.
- It is recommended to turn off block updates as they can break the converter. You can do this by setting the randomTickSpeed to 0 ````/gamerule randomtickspeed 0```` and by downloading [WorldGuard](https://dev.bukkit.org/projects/worldguard/files). The reason this happens is because, for example, if a chunk loads and there is a floating vine, it could break. If the converter then tries to convert that vine and it's no longer there, it'll throw an exception.

### Program Steps
##### __CLI portion__:
1. Download the program's Jar file.
2. Open the command line interface (CLI) or terminal on your computer.
3. Navigate to the directory where the Jar file is located.
4. Execute the following command in the CLI or terminal:

```java -jar CC-1.18.2-Converter.jar <path to input> <path to output> <minY> <maxY> <offset> [threads]```

    - Replace the <path to input> with the path to your input world file folder.
    - Replace the <path to output> with the path to the folder where you want to save the converted world.
    - Replace <minY> with the minimum Y value of the 1.18.2 world.
    - Replace <maxY> with the maximum Y value of the 1.18.2 world.
    - Replace <offset> with the desired offset in blocks/meters.
    - Add an optional [threads] parameter to specify the number of threads to use. By default, the program will use one thread.
5. Press the Enter key to run the command.
6. The program will begin running and convert your world. The progress will be displayed in the CLI or terminal.
7. Once the conversion is complete, the converted world will be saved in the output folder you specified. The program will display the total time taken to complete the conversion. 
___
##### __Paper portion__:
1. Download [PaperMC](https://papermc.io/) 1.18.2
2. Download [Terra+-](https://github.com/BTE-Germany/TerraPlusMinus) and put it into the plugins folder on PaperMC, make sure to follow the installation guide.
3. Enable the extended height datapack for Terra+- in the config.
4. [Configure the Datapack](#datapack-configuration) as to keep it compatible with settings used in the CLI portion.
5. Head over to the Terra+- config in /plugins/TerraPlusMinus/config.yml and set the offset to what you set in the CLI portion.
6. Run the server to start world loading and ensure all is working
7. Download the converter plugin and move it into your /plugins/ folder
8. Run the server then stop it after it loads fully
9. Move the *post-processing* (and **maps** folder, if your world has filled maps) folder from your converted world folder to /plugins/Converter
10. Set the world in the config in that folder to the same name of the world on the server
11. Head over to the world folder (i.e. /world/) and put the *region* folder from the converted world folder in there
12. Start the server. Whenever you teleport to those regions, the converted region will automatically be loaded in.

And that's it! You have successfully used the program to convert your Minecraft world from version 1.18.2.

---
### __Datapack Configuration:__
1. To generate the Datapack you need to enabled/disable the server once or twice.
2. Find the datapack in the `world/datapacks` directory.
3. Unzip the datapack.
4. Edit the `pack.mcmeta` and alter the [pack format](https://minecraft.fandom.com/wiki/Data_pack#Pack_format) to the version of your server.
5. Navigate to `overworld.json` in `data/minecraft/dimension_type/`.
6. Set `min_y` to the values used in the CLI portion.
7. Set `logical_height` and `height` to `max_y - min_y`.
> Example: if `max_y = 1952` and `min_y = -64` then `logical_height/height = 1952 - -64 = 2016`
8. Zip the datapack and make sure it has the same structure as the original zip file.
9. Add it back to the datapack folder.

## Authors

- Author: [@LM-Wolfert](https://www.github.com/LM-Wolfert)
- Adding entity support and major improvements [@DavixDevelop](https://github.com/DavixDevelop)
- Adding offsets and new documentation: [@LordKnish](https://github.com/LordKnish)
