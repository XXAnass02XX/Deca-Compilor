class Color {
    boolean bit1 = true;
    boolean bit2 = true;
    protected int index = 124;
    void setBlack() {
        this.bit1 = false;
        this.bit2 = false;
        this.index = 127;
    }
    void setDark() {
        this.bit1 = false;
        this.bit2 = true;
        this.index = 127;
    }
    void setLight() {
        this.bit1 = true;
        this.bit2 = false;
        this.index = 126;
    }
    void setWhite() {
        this.bit1 = true;
        this.bit2 = true;
        this.index = 124;
    }
    boolean isWhite() {
        return this.bit1 && this.bit2;
    }
    boolean isBlack() {
        return !this.bit1 && !this.bit1;
    }
    int getTileIndex() {
        return this.index;
    }
    boolean isSameColor(Color color) {
        return this.bit1 == color.bit1 && this.bit2 == color.bit2;
    }

}
class BackgroundMapMod {
    protected int color = 124;
    protected boolean user = false;
    protected boolean changed = true;
    int WIDTH = 32;
    int HEIGHT = 32;

    void setColor(int c) {
        if (this.color != c) {
            this.changed = true;
            this.color = c;
        }
    }


    void setUser() {
        this.changed = true;
        this.user = true;
    }
    boolean hasChanged() {
        return changed;
    }
    void setStateUpdated() {
        this.changed = false;
    }

    int getColor() {
        //this.color = 126;
        return this.color;
    }
}
class DrawEvent {
    protected int tileIndex;
    protected int x;
    protected int y;
    protected DrawEvent next = null;

    void setTileIndex(int tileIndex) {
        this.tileIndex = tileIndex;
    }

    void setX(int x) {
        this.x = x;
    }

    void setY(int y) {
        this.y = y;
    }

    int getTileIndex() {
        return tileIndex;
    }

    int getX() {
        return x;
    }

    int getY() {
        return y;
    }

    DrawEvent getNext() {
        return next;
    }

    void init(int index, int x, int y) {
        this.tileIndex = index;
        this.x = x;
        this.y = y;
    }
    void setNext(DrawEvent event) {
        this.next = event;
    }
    boolean hasNext() {
        return this.next != null;
    }
}
class DrawEventList {
    protected DrawEvent first = null;
    protected DrawEvent last = null;
    int nextIndex = 0;
    int test = 1;
    int size = 20;
    Utils utils = new Utils();

    void init() {
        int i = 0;
        DrawEvent event;
        this.nextIndex = 0;
        while (i < size) {
            event = new DrawEvent();
            this.addInit(event);
            i = i + 1;
        }
        // event.setNext(first);
    }
    /*private*/void set(int i, int tileIndex, int x, int y) {
        int k = 0;
        DrawEvent current = first;
        while (k < i) {
            current = current.getNext();
            k=k+1;
        }
        current.setX(x);
        current.setY(y);
        current.setTileIndex(tileIndex);
    }
    void add(int tileIndex, int x, int y) {
        if (nextIndex == this.size + 1) {
            nextIndex = 0;
        }
        set(nextIndex, tileIndex, x, y);
        nextIndex = nextIndex + 1;
    }
    /*private*/ void addInit(DrawEvent event) {
        if (this.first == null) {
            this.first = event;
            this.last = event;
        } else {
            this.last.setNext(event);
            this.last = event;
        }
    }
    DrawEvent getFirst() {
        return this.first;
    }
    void drawList(){
        int i = 0;
        int x;
        int y;
        int tileIndex;
        DrawEvent current = first;
        boolean currentNotNull = true;
        while (i < nextIndex) {
            if (current == null) {
                currentNotNull = false;
                i = nextIndex;
                nextIndex = 0;
            }
            if (currentNotNull == true) {
                tileIndex = current.getTileIndex();
                x = current.getX();
                y = current.getY();
                utils.pushInTileMap(x, y, tileIndex);
                current = current.getNext();
                i = i + 1;
            }
        }
        nextIndex = 0;
    }
    void debug(int i) asm(
            "ld hl, sp + 4
            ld a, [hl]
            loop:
            jp loop
            "
            );
}

class Utils {

    void setBackGroundInTileMap(int index) asm (
        "
        ld hl, sp + 4
        ld e, [hl]
        ;ld e, $7e
        ld hl, $9800
        ld bc, $240
        setBackGroundInTileMapLoop:
            ld [hl], e
            inc hl
            dec bc
            ld a, b
            or a, c
            jp nz, setBackGroundInTileMapLoop ; Jump to COpyTiles, if the z flag is not set. (the last operation had a non zero result)
        ret
        "
    );
    void pushInTileMap(int x, int y, int tileIndex) asm(
            "
            ; a = y
            ld hl, sp + 6
            ld a, [hl]
            ;ld a, 10

            ; b = x
            ld hl, sp + 4
            ld b, [hl]
            ;ld b, 10

            ; c = value
            ld hl, sp + 8
            ld c, [hl]
            ;ld c, $7f

            PushInTileMap:

            ; hl c est la premiere adresse de la map
            ld hl, $9800
            ; si y == 0 on passe à la boucle des colonnes
            and a, a
            jp z, yEqualsZero

            ; a = y
            ;ld hl, sp + 6
            ;ld a, [hl]
            ;ld a, 10


            ; de = 32
            ld e, 32
            ld d, 0

            ; on incremente hl de y * 32
            yLinesLoop:
                add hl, de
                dec a
                jp nz, yLinesLoop

            yEqualsZero:
            ld d, 0
            ld e, b
            add hl, de ;hl += x;
    ;call waitVDrawLoop
            call WaitForOneVBlank
            ld [hl], c
            ret
            "
            ); // TODO VRAIMENT PAS SÛR

    int random() asm(
            "
            call rand
            ld l, a
            ld h, c
            ret
            "
            );


}