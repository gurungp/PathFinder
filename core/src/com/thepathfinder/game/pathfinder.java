package com.thepathfinder.game;


import com.badlogic.gdx.ApplicationAdapter;
import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.Input;
import com.badlogic.gdx.InputProcessor;
import com.badlogic.gdx.graphics.Color;
import com.badlogic.gdx.graphics.GL20;
import com.badlogic.gdx.graphics.OrthographicCamera;
import com.badlogic.gdx.graphics.Texture;
import com.badlogic.gdx.graphics.g2d.Sprite;
import com.badlogic.gdx.graphics.g2d.SpriteBatch;
import com.badlogic.gdx.graphics.glutils.ShapeRenderer;
import com.badlogic.gdx.maps.MapProperties;
import com.badlogic.gdx.maps.tiled.TiledMap;
import com.badlogic.gdx.maps.tiled.TiledMapRenderer;
import com.badlogic.gdx.maps.tiled.TiledMapTileLayer;
import com.badlogic.gdx.maps.tiled.TmxMapLoader;
import com.badlogic.gdx.maps.tiled.renderers.OrthogonalTiledMapRenderer;
import com.badlogic.gdx.math.CatmullRomSpline;
import com.badlogic.gdx.math.Vector2;
import com.badlogic.gdx.math.Vector3;
import com.badlogic.gdx.utils.viewport.FitViewport;

import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.Random;


public class pathfinder extends ApplicationAdapter implements InputProcessor {
    Texture texture;
    TiledMap tiledMap;
    OrthographicCamera camera;
    TiledMapRenderer tiledMapRenderer;
    SpriteBatch sb;
    Sprite sprite;
    FitViewport fitViewport;
    ShapeRenderer grid;
    ArrayList<GraphInfo> openedList, closedList, pathway;
    HashMap<Integer,CellInfo> hashMap;

    int StartPos = 0;
    int endNodeX = 0;
    int endNodeY = 0;
    int destPositionI = 0;
    int destPositionJ = 0;


    float w = 0.0f;
    float h = 0.0f;

    float mapHeight, lastPosY = 0.0f;
    float mapWidth, lastPosX = 0.0f;

    float camLastPosX, camLastPosY = 0.0f;

    float destinationX = 0;
    float destinationY = 0;

    boolean traverse = false;
    boolean goalFound = false;

    float speed = 1.0f;
    float current = 0;
    float t=0;

    float spriteCentreX, spriteCentreY = 0;
    float cameraDiffX, cameraDiffY = 0;
    float squareWidth = 0;
    float squareHeight = 0;

    GraphInfo graph[][] = new GraphInfo[32][32];
    CellInfo cells[][] = new CellInfo[32][32];


    Vector2 out = new Vector2();
    Vector2 tmp = new Vector2();
    Vector2[] points = {
            new Vector2(0,0),
            new Vector2(0,0)
    };

    @Override
    public void create () {

        w = Gdx.graphics.getWidth();
        h = Gdx.graphics.getHeight();

        System.out.println("Width : " + String.valueOf(w));
        System.out.println("Height : " + String.valueOf(h));

        grid = new ShapeRenderer();

        camera = new OrthographicCamera();
        camera.setToOrtho(false,w,h);
        camera.update();

        camLastPosX = camera.position.x;
        camLastPosY = camera.position.y;

        fitViewport = new FitViewport(w,h,camera);

        Random rand = new Random();
        int ran = rand.nextInt(4) + 1;

        String map = "tile" + String.valueOf(ran) + ".tmx";

        tiledMap = new TmxMapLoader().load(map);
        MapProperties mapProperties = tiledMap.getProperties();
        tiledMapRenderer = new OrthogonalTiledMapRenderer(tiledMap);
        Gdx.input.setInputProcessor(this);

        sb = new SpriteBatch();
        texture = new Texture(Gdx.files.internal("sprite.png"));
        sprite = new Sprite(texture);
        spriteCentreX = sprite.getWidth()/2;
        spriteCentreY = sprite.getHeight()/2;


        mapWidth = (mapProperties.get("width",Integer.class)) * (mapProperties.get("tilewidth",Integer.class));
        mapHeight = (mapProperties.get("height",Integer.class)) * (mapProperties.get("tileheight",Integer.class));

        squareWidth = mapWidth/32;
        squareHeight = mapHeight/32;

        //Initialize the Opened List, Closed List, hashMap and Pathway
        openedList = new ArrayList<GraphInfo>();
        closedList = new ArrayList<GraphInfo>();
        pathway = new ArrayList<GraphInfo>();
        hashMap = new HashMap<Integer, CellInfo>();


        init(); // Initialize

        System.out.println("MAP Width : " + mapWidth);
        System.out.println("MAP Height : " + mapHeight);

    }

    @Override
    public void render () {

        Gdx.gl.glClearColor(0.5f, 0.5f, 0.5f, 1);
        Gdx.gl.glBlendFunc(GL20.GL_SRC_ALPHA, GL20.GL_ONE_MINUS_SRC_ALPHA);
        Gdx.gl.glClear(GL20.GL_COLOR_BUFFER_BIT);

        tiledMapRenderer.setView(camera);
        tiledMapRenderer.render();

        RenderGrid();

        // System.out.println("Square width: " + (squareWidth));
        // System.out.println("Square height: " + (squareHeight));
        // System.out.println("camera position X : " + camera.position.x);
        // System.out.println("camera position Y : " + camera.position.y);
        //-------------------------------------------------

        MovePlayer();


        camera.update();


    }

    public void MovePlayer(){

       if(pathway.isEmpty()){
           sb.begin();
           sb.draw(sprite, points[0].x,points[0].y);
           sb.end();
       }else{
           if(pathway.size()>1){
               points[0].set(pathway.get(pathway.size()-1).getX(),pathway.get(pathway.size()-1).getY());
               points[1].set(pathway.get(pathway.size()-2).getX(),pathway.get(pathway.size()-2).getY());
               pathway.remove(pathway.size()-1);

               CatmullRomSpline.calculate(out, t, points, false, tmp);
               CatmullRomSpline.derivative(out, t, points, false, tmp);
               current=0;
           }else{}

           if(traverse){

               current += (Gdx.graphics.getDeltaTime() * speed);

               if(current >= 1)
                   current -= 1;

               float place = current * points.length; //

               Vector2 first = points[(int) place];
               Vector2 second;

               if(((int)place+1) < points.length){
                   second = points[(int)place +1];
               }else{
                   second = points[points.length-1];
               }

               t = place - ((int)place);   //

               sb.begin();

               sprite.setPosition(first.x + (second.x - first.x) * t, first.y + (second.y - first.y) * t);
               sb.draw(sprite, first.x + (second.x - first.x) * t, first.y + (second.y - first.y) * t);


/*
               if(w-sprite.getX()<=300){
                   if(lastPosX>sprite.getX()){
                       cameraDiffX = camLastPosX - (lastPosX - (first.x + (second.x - first.x) * t));
                       camera.position.x  = cameraDiffX;
                   }
                   else{
                       cameraDiffX = camLastPosX + ((first.x + (second.x - first.x) * t) - lastPosX );
                       camera.position.x = cameraDiffX;
                   }
               }
               else{
               }

               if(h-sprite.getY()<=300){
                   if(lastPosY>sprite.getY()){
                       camera.position.y = camLastPosY - (lastPosY - (first.y + (second.y - first.y) * t));
                   }
                   else{
                       camera.position.y = camLastPosY + ((first.y + (second.y - first.y) * t) - lastPosY);
                   }
               }else{}
               sb.setProjectionMatrix(camera.combined);*/
               sb.end();

               lastPosX = sprite.getX();
               lastPosY = sprite.getY();
            //   camLastPosX = camera.position.x;
           //    camLastPosY = camera.position.y;

               //System.out.println("Time: " + t);

               if(t>=0.95)
               {
                   traverse=false;
                   points[0].x=destPositionI;
                   points[0].y=destPositionJ;
                   System.out.println("points[0].x : " + points[0].x + " points[0].y : " + points[0].y );
                   System.out.println("points[1].x : " + points[1].x + " points[1].y : " + points[1].y );

                   //  points[0].x = destinationX-32;
                //   points[0].y = destinationY-32;

               }

               //  System.out.println("The current sprite position = (" + sprite.getX() +" , " + sprite.getY() + ")");

           }else{
               sb.begin();
               sb.draw(sprite, points[1].x,points[1].y);
               sb.end();
                //   System.out.println("The current sprite position = (" + sprite.getX() +" , " + sprite.getY() + ")");
           }
       }


    }

    public void RenderGrid(){

        grid.begin(ShapeRenderer.ShapeType.Line);
        //grid.setColor(Color.WHITE);

        for(int j=0;j<32;j++){
            for(int i=0;i<32;i++){
                grid.setColor(graph[i][j].c);
                grid.box(((squareWidth*i) - (camera.position.x - (w/2))), // so as to move along with the camera movement in X direction
                        ((mapHeight/32+(squareHeight*j)) - 32) - ((camera.position.y) - (h/2)), // as well as Y direction
                        0,
                        squareWidth,
                        squareHeight,
                        0);
             //   System.out.println("Camera Position X " + camera.position.x);
            }

        }

        grid.end();
/*
        if(goalFound){
           // System.out.println("New GOAL FOUND : YES");
            int parent = 9999;
            int testEndnodeX = endNodeX;
            int testEndnodeY = endNodeY;


            while(parent!=StartPos){
                // System.out.print(" New : Start number: " + StartPos);
               // System.out.print(" New :  Destination number: " + graph[endNodeX][endNodeY].number);
               // System.out.println(" New :  Parent number: " + graph[endNodeX][endNodeY].getParentNode() + " :: (i,j) = " + "(" + endNodeX + ", " + endNodeY + ")");

                grid.begin(ShapeRenderer.ShapeType.Line);
                grid.setColor(Color.RED);
                grid.box(graph[endNodeX][endNodeY].getX(),graph[endNodeX][endNodeY].getY(),0,squareWidth,squareHeight,0);
                grid.end();

                parent = graph[endNodeX][endNodeY].getParentNode();
                endNodeX = hashMap.get(parent).getI();
                endNodeY = hashMap.get(parent).getJ();

            }
            endNodeX = testEndnodeX;
            endNodeY = testEndnodeY;

        }else{
           // System.out.println("GOAL FOUND : NO");
        }
*/
    }

    public void init(){
        //------------------------- Initialize the coordinates of the graph and block positions (non traversable paths) --------------------------
        TiledMapTileLayer layer = (TiledMapTileLayer) tiledMap.getLayers().get(1);
       for(int i=0;i<hashMap.size();i++){
           hashMap.remove(i);
       }
            hashMap.clear();

        for(int i=0;i<openedList.size();i++)
        {
            openedList.remove(i);
        }
        openedList.clear();

        for(int i=0;i<closedList.size();i++){
            closedList.remove(i);
        }
        closedList.clear();

        for(int i=0;i<pathway.size();i++){
            pathway.remove(i);
        }
        pathway.clear();


    /*    if(!hashMap.isEmpty()){
            hashMap.clear();
        }else{}

        if(!openedList.isEmpty())
            openedList.clear();

        if(!closedList.isEmpty())
            closedList.clear();

        if(!pathway.isEmpty())
            pathway.clear();*/

        int count=0;

        for(int j=0;j<32;j++){
            for(int i=0;i<32;i++){
                graph[i][j] = new GraphInfo((squareWidth*i) - (camera.position.x - (w/2)),  (((mapHeight/32+(squareHeight*j)) - 32) - ((camera.position.y) - (h/2))),count++ );
                cells[i][j] = new CellInfo(i,j,graph[i][j].number,graph[i][j].F);
                hashMap.put(graph[i][j].number,cells[i][j]);
            //  System.out.println("Map Height: " + mapHeight + " Camera Position : " + camera.position.y + " h: " + h);
            //    System.out.println(" cell number: " + graph[i][j].number);
                TiledMapTileLayer.Cell cell = layer.getCell(i,j);

                if(cell!=null){ // if it is a block
                   // System.out.println("X: " + i + "Y: " + j);
                    graph[i][j].setBlock(true);
                    graph[i][j].setClosed(true);
                }else{}
                //     System.out.println(layer.getCell(i,j).toString());

            //    System.out.println("Coord : (" + i + ", " + j +") " + " X : " + (graph[i][j].getX()) + " , Y: " + graph[i][j].getY());

            }
        }
        //-------------------------------------------------------------------------------------------------------------------------------------------

    }


    public void calculatePath(){
        // add starting posiion which is the first grid [0][0] to closed list
      //  System.out.println("Current Grid Position is (" + currentGridPositionX() + "," + currentGridPositionY() + ")");

        ScoreOperation(); // addition and removal of nodes from the opened and closed list
        buildPath();

    }

    public void ScoreOperation(){

          //  System.out.println("STEP 1");
          //  int i=currentGridPositionX();
           // int j=currentGridPositionY();

            int i = (int)points[0].x;
            int j = (int)points[0].y;

            StartPos = graph[i][j].number;

            System.out.println("db New Starting first : " + StartPos + " i: " + i +", " + "j:" + j);

                openedList.add(graph[i][j]);
                while(!openedList.isEmpty() || !goalFound)
                {


                    i = hashMap.get(openedList.get(0).number).getI(); // adding the first cell of the arraylist which has the lowest F value and getting its position [i,j].
                    j = hashMap.get(openedList.get(0).number).getJ();

                    System.out.println("First I : " + i);
                    System.out.println("First J : " + j);

                   // System.out.println("New : opened list length " + openedList.size() + " First element number: " + openedList.get(0).number + " Last element number: " + openedList.get(openedList.size()-1).number);
                   // System.out.println("New : first opened list x " + "x: " + openedList.get(0).getX() + ", y: " +openedList.get(0).getY() );
                  /*  System.out.println("db New Starting from " + "i: " + i + ", j: " +j );
                    for(int k= 0; k < openedList.size();k++){
                        System.out.println((k+1) + " : : Opened Elements number: " + openedList.get(k).number + " i:"  + hashMap.get(openedList.get(k).number).getI() + " j:"  + hashMap.get(openedList.get(k).number).getJ() + " F value : " + (openedList.get(k).F));
                    }*/

                    closedList.add(graph[i][j]); // add current node to closed list
                    openedList.remove(0); // remove current node from opened list


                    graph[i][j].setClosed(true);
                  //  graph[i][j].setOpened(false);


                    // adding adjacent nodes to the opened list
                    int parent = graph[i][j].number;
                    int currentG = graph[i][j].G;

                    score(i,j+1,10,parent,currentG);
                    score(i,j-1,10,parent,currentG);
                    score(i+1,j,10,parent,currentG);
                    score(i-1,j,10,parent,currentG);
                    score(i+1,j+1,14,parent,currentG);
                    score(i+1,j-1,14,parent,currentG);
                    score(i-1,j+1,14,parent,currentG);
                    score(i-1,j-1,14,parent,currentG);

                    Collections.sort(openedList);
                }

    }

    public void score(int i, int j,int value, int parent,int curG){
        if(i<0 || i > 31 || j < 0 || j >31){
            // if its out of bounds do nothing
        }
        else{

            if(graph[i][j].isBlock()){
                closedList.add(graph[i][j]);
                graph[i][j].setClosed(true);
            }
            else {


                if (graph[i][j].isOpened() && !graph[i][j].isBlock() && !graph[i][j].isClosed()) {
                    //   System.out.print(" New : i:" + i + "j:" + j + " is already Opened ");
                    if (graph[i][j].G >= value + curG) {
                        //        System.out.println("New Setting parent to : "  + parent);
                        graph[i][j].setParentNode(parent);
                        graph[i][j].G = value + curG;
                        graph[i][j].F = graph[i][j].G + graph[i][j].H;
                    } else { // do nothing
                    }

                } else {
                }

                if (!graph[i][j].isOpened() && !graph[i][j].isBlock() && !graph[i][j].isClosed()) {
                    // first set its parent
                    graph[i][j].colorSet(Color.WHITE);
                    graph[i][j].setParentNode(parent);
                    //   System.out.println("New Opening i: " + i + ", j: " + j);

                    graph[i][j].setOpened(true); // set the current cell to opened, true

                    if (openedList.contains(graph[i][j])) {
                        //  dont add if its already there
                    } else {
                        openedList.add(graph[i][j]); // add this adjacent valid cell to the opened list
                    }

                    //calculate G value
                    graph[i][j].G += value;

                    // calculating the Heuristic (H) value
                    int xH = Math.abs(i - destGridPositionX());
                    int yH = Math.abs(j - destGridPositionY());

                    graph[i][j].H = (Math.min(xH, yH) * 14) + (Math.abs(xH - yH) * 10);

                    //calculating the F value
                    graph[i][j].F = graph[i][j].G + graph[i][j].H;

                    System.out.println("G: " + graph[i][j].G + " H: " + graph[i][j].H + " F: " + graph[i][j].F + " Parent: " + "(" + hashMap.get(graph[i][j].number).getI() + ", " + hashMap.get(graph[i][j].number).getI() + ")");
                    //  System.out.println("----------------------------------------------------------------------------------");
                    System.out.println();

                    if (i == destGridPositionX() && j == destGridPositionY()) {
                        System.out.println("EndNode Destination Found");
                        endNodeX = destGridPositionX();
                        endNodeY = destGridPositionY();
                        System.out.println("EndNode Destination Found EndNodeX: " + endNodeX);
                        System.out.println("EndNode Destination Found EndNodeY: " + endNodeY);
                        System.out.println("EndNode Parent Node: " + graph[endNodeX][endNodeY].getParentNode());

                        destPositionI = endNodeX;
                        destPositionJ = endNodeY;

                        System.out.println("points[1].x : " + points[1].x + " points[1].y : " + points[1].y);

                        goalFound = true;
                        // break;
                    }

                } else {
                }

            }
        }
    }

    public int currentGridPositionX(){
        System.out.println("New : Current sprite pos x " + sprite.getX());
        return ((int) ((sprite.getX())/32 ));
    }
    public int currentGridPositionY(){
        System.out.println("New : Current sprite pos y " + sprite.getY());
        return ((int) (sprite.getY())/32 );
    }
    public int destGridPositionX(){
        System.out.println("Destination x CHECKING" + ((int) destinationX/32 ));
        //System.out.println("Clicked x CHECKING" + );

        return ((int) destinationX/32 );
    }
    public int destGridPositionY(){
        System.out.println("Destination y " + ((int) destinationY/32 ));
        return ((int) destinationY/32 );
    }

    public void buildPath(){
        int parent = 9999;

      /*  for(int i=0;i<32;i++){
            for(int j=0;j<32;j++){
                System.out.println(": TEST GRID NUMBER ( " + i + "," + j + ") " + graph[i][j].number);
                System.out.println(": TEST GRID X,Y ( " + graph[i][j].getX() + "," + graph[i][j].getY() + ") " );
            }
        }*/

        while(parent!=StartPos){

                System.out.println(" EndNode Start number: " + StartPos);
                // System.out.print(": New :  Destination number: " + graph[endNodeX][endNodeY].number);
                // System.out.println(": New :  Parent number: " + graph[endNodeX][endNodeY].getParentNode() + " :: (i,j) = " + "(" + endNodeX + ", " + endNodeY + ")");

                System.out.println("EndNode X " + endNodeX);
                System.out.println("EndNode Y " + endNodeY);

                graph[endNodeX][endNodeY].colorSet(Color.BLUE);

                GraphInfo temp = new GraphInfo(graph[endNodeX][endNodeY].getX(),graph[endNodeX][endNodeY].getY(),graph[endNodeX][endNodeY].number);
                pathway.add(temp);
                parent = graph[endNodeX][endNodeY].getParentNode();

                System.out.println("EndNode Parent " + parent);
                if(parent==StartPos)
                     break;

                endNodeX = hashMap.get(parent).getI();
                endNodeY = hashMap.get(parent).getJ();

                if(parent==0)
                {
                    System.out.println("EndNode Zero");
                    break;
                }

        }

    }

    @Override
    public boolean keyDown(int keycode) {
        return false;
    }

    @Override
    public boolean keyUp(int keycode) {
        if(keycode == Input.Keys.LEFT)
            camera.translate(-32,0);
            grid.translate(-32,0,0);
        if(keycode == Input.Keys.RIGHT)
            camera.translate(32,0);
            grid.translate(32,0,0);
        if(keycode == Input.Keys.UP)
            camera.translate(0,-32);
            grid.translate(0,-32,0);
        if(keycode == Input.Keys.DOWN)
            camera.translate(0,32);
            grid.translate(0,32,0);
        if(keycode == Input.Keys.NUM_1)
            tiledMap.getLayers().get(0).setVisible(!tiledMap.getLayers().get(0).isVisible());
        if(keycode == Input.Keys.NUM_2)
            tiledMap.getLayers().get(1).setVisible(!tiledMap.getLayers().get(1).isVisible());
        return false;
    }

    @Override
    public boolean keyTyped(char character) {

        return false;
    }

    @Override
    public boolean touchDown(int screenX, int screenY, int pointer, int button) {

       if(traverse){ // do not consider while its moving
        return false;
       }else{

           Vector3 clickCoordinates = new Vector3(screenX,screenY,0);
           Vector3 position = camera.unproject(clickCoordinates);

           destinationX = ((int) position.x);
           destinationY = ((int) position.y);

           System.out.println(": Clicked X:" + destinationX);
           System.out.println(": Clicked Y:" + destinationY);


           if(graph[destGridPositionX()][destGridPositionY()].isBlock() || (destGridPositionX() == points[0].x  && destGridPositionY() == points[0].y)){

           }else{
               init(); // to restart the same process
               goalFound = false;
               calculatePath();
               traverse = true;
           }

           return false;
       }
    }

    @Override
    public boolean touchUp(int screenX, int screenY, int pointer, int button) {
        return false;
    }

    @Override
    public boolean touchDragged(int screenX, int screenY, int pointer) {
        return false;
    }

    @Override
    public boolean mouseMoved(int screenX, int screenY) {
        return false;
    }

    @Override
    public boolean scrolled(int amount) {
        return false;
    }
}