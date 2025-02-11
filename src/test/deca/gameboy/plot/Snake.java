class SnakeCell{
    SnakeCell nextSnakeCell = null;
    SnakeCell previousSnakeCell = null;
    int x ;
    int y ;
    void initSnakeCase(SnakeCell nextSnakeCell, SnakeCell previousSnakeCell, int x ,int y){
        this.nextSnakeCell = nextSnakeCell;
        this.previousSnakeCell = previousSnakeCell;
        this.x = x;
        this.y = y;
    }
    int getX(){
        return this.x;
    }
    int getY(){
        return this.y;
    }
    void setX(int x){
        this.x=x;
    }
    void setY(int y){
        this.y=y;
    }
    SnakeCell getNext(){
        return this.nextSnakeCell;
    }

    SnakeCell getPrevious(){
        return this.previousSnakeCell;
    }

    void setNext(SnakeCell snakeCell){
        this.nextSnakeCell = snakeCell;
    }

    void setPrevious(SnakeCell snakeCell){
        this.previousSnakeCell=snakeCell;
    }
}
class Snake{
    protected SnakeCell head;
    protected SnakeCell tail;
    protected int xPositionHead;
    protected int xPreviousPositionHead;
    protected int yPositionHead;
    protected int yPreviousPositionHead;
    protected int maxX;
    protected int maxY;
    protected Reward reward;
    //protected int color;
    //protected int colorReward;
    protected GameBoy gb;

    protected int tailTile = 0;
    protected int rightHeadTile = 1;
    protected int leftHeadTile = 2;
    protected int downHeadTile = 3;
    protected int upHeadTile = 4;
    protected int appleTile = 9;

    int compteurAdder = 0;
    int adder = 0;



    //protected boolean printFirst = true;
    int VerticalDirection = 0;
    int HorizontalDirection = 1;
    int direction;
    int score = 0;
    int getDirection() {
        return direction;
    }

    void initSnake( GameBoy g, int xH, int yH, int xMax, int yMax, int xR, int yR){
        int compteurWhile = 8;
        this.gb = g;

        //this.color = 126;
        //this.colorReward = 124;
        this.head = new SnakeCell();
        this.tail = new SnakeCell();
        this.xPositionHead=xH;
        this.yPositionHead=yH;
        this.maxX=xMax;
        this.maxY=yMax;
        this.head.initSnakeCase(null,this.tail,xH,yH);
        this.tail.initSnakeCase(this.head,null,xH-1,yH);
        this.direction = this.HorizontalDirection;
        this.reward=new Reward();
        this.reward.initReward(xR, yR,xMax,yMax);
        this.gb.setColor(upHeadTile, xH, yH);
        this.gb.setColor(tailTile, xH-1, yH);
        this.gb.setColor(appleTile, xR, yR);
        gb.updateScreen();
        while (compteurWhile <= 19) {
            gb.setColor(10, compteurWhile, 1);
            compteurWhile  = compteurWhile + 1;
        }

        print("score", 0, 1);
        this.gb.printNumber(this.score,6,1);
    }

    int getHeadX(){
        return xPositionHead;
    }

    int getHeadY(){
        return yPositionHead;
    }


    boolean move(int i){
            boolean a;
            int backgroundColor = gb.getBackgroundColor();
            SnakeCell newHead;
            int tile = upHeadTile;
            xPreviousPositionHead = xPositionHead;
            yPreviousPositionHead = yPositionHead;
            if(i==0){
                goUp();
                tile = upHeadTile;
            }
            else if (i==1){
                goDown();
                tile = downHeadTile;
            }
            else if(i==2){
                goRight();
                tile = rightHeadTile;
            }
            else if(i==3){
                goLeft();
                tile = leftHeadTile;
                }
            /*if(printFirst){
                print("score ", 2, 0);
                gb.printNumber(this.score, 3, 1);
                this.printFirst = false;
            }*/
            compteurAdder  = compteurAdder + 1;
            if (compteurAdder == 2) {
                compteurAdder = 0;
                if (adder == 0) {
                    adder = 4;
                } else {
                    adder = 0;
                }
            }
            a = this.collusion(this.xPositionHead, this.yPositionHead);
            gb.setColor(tile + adder, this.xPositionHead, this.yPositionHead);
            gb.setColor(tailTile, this.xPreviousPositionHead, this.yPreviousPositionHead);
            //si la position du head est celle de la reward
            if(xPositionHead==this.reward.x && yPositionHead==this.reward.y){
                //print("score", 2, 0);
                score = score + 1;
                gb.printNumber(score, 6, 1);
                newHead=new SnakeCell();
                newHead.initSnakeCase(null, null,xPositionHead,yPositionHead);
                this.head.setNext(newHead);
                newHead.setPrevious(this.head);
                this.head=newHead;
                this.head.setNext(null);
                this.reward.changeParams();
                while (!this.collusion(this.reward.x, this.reward.y)){
                    this.reward.changeParams();
                }
                gb.setColor(appleTile, this.reward.getX(), this.reward.getY());
            }
            else{
                gb.setColor(backgroundColor, this.tail.getX(), this.tail.getY());
                newHead = this.tail;
                this.tail = this.tail.getNext();
                this.head.setNext(newHead);
                newHead.setPrevious(this.head);
                this.head = newHead ;
                this.head.setNext(null);
                this.tail.setPrevious(null);
                this.head.setX(this.xPositionHead);
                this.head.setY(this.yPositionHead);
            }
            return a;
    }

    // Le modulo c'est pour que si on sort d'un coté (exp le coté droit) on sort depuis l'autre coté ( coté gauche)
    void goUp(){
        this.direction = this.VerticalDirection;
        this.yPositionHead = (this.yPositionHead - 1 + maxY)% maxY;
        if(this.yPositionHead < 2){
            this.yPositionHead = maxY -1;
        }
    }
    void goDown(){
        this.direction = this.VerticalDirection;
        this.yPositionHead = (this.yPositionHead + 1) %maxY;
        if(this.yPositionHead < 2){
            this.yPositionHead = 2;
        }
    }
     void goRight(){
        this.direction = this.HorizontalDirection;
        this.xPositionHead = (this.xPositionHead +1 ) % maxX;
     }
     void goLeft(){
        this.direction = this.HorizontalDirection;
        this.xPositionHead = (this.xPositionHead -1 + maxX) %maxX;
     }
     boolean collusion(int x, int y){
        SnakeCell current = this.tail;
        while(current != null){
            if(current.getX() == x && current.getY() == y){
                return false;
            }
            current = current.getNext();
        }
        return true ;
     }

     void printSnake(){
        SnakeCell current = this.head;
        while (current != null) {
            //print(current.getX());
            //println(current.getY());
            current = current.getPrevious();
        }
     }
}
class Reward {
    int x ;
    int y ;
    int maxX = 20;
    int maxY = 18;
    Utils utils = new Utils();

    int getX(){
        return this.x;
    }

    int getY(){
        return this.y;
    }

    void initReward(int x,int y, int maxX , int maxY){
        this.x = x;
        this.y = y;
        this.maxX = maxX;
        this.maxY = maxY;
    }
    void changeParams(){
        this.x = this.utils.random() % this.maxX;
        this.y = this.utils.random() % this.maxY;
        if(this.y < 2) {
            this.y = 2;
        }
//        print("reward :");
//        print(this.x);
//        print("-");
//        println(this.y);
    }
}

