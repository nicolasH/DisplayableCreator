### Tile Creator App

This java application was created to transform images into sqlite db of tiles representing the image at different levels of zoom (1:1 1:1/2 1:1/4 ...).

It then allows one to share the created db over the network.

It uses jmdns for announcing the service on the lan and Jetty for the file serving.

You can run it [from the displayator website as a jnlp][jnlp] (java webstart).

### Usage :

# Phase 1 : Tile set creation.

- Select the image you want to tile by clicking on the top browse button of the app. The tiling process will start immediately.
- You can choose the title of the image as it will appear in the iphone app.
- You can choose the name of the file that will be saved (by default the same name as the original file with a different extension).
- You *have* to choose the directory in which to save the files.
- When the tiling is finished *and* the save directory is selected, the finalize button should become clickable. When you click it, the newly created tile set willl appear in the list of files available for sharing bellow. 

# Phase 2 : Sharing the tile sets.
Your iphone needs to be on the same network as the computer on which you run the tile set creator for sharing to work.

- Click "start sharing".  
- On your iPhone/ iPod launch the app and then go in the "download images" view. The availables images should appear quickly. You can then download them.

[jnlp]:http://www.displayator.com/TileCreatorApp/jnlp/tile-creator-app.jnlp