#include "Utils.java"

class GameBoy {
    DrawEventList drawEvents = new DrawEventList();
    int WIDTH = 20;
    protected int pixelWidth = 160;
    int HEIGHT = 18;
    protected int pixelHeight = 144;



    int UP_KEY = 64;
    int DOWN_KEY = 128;
    int LEFT_KEY = 32;
    int RIGHT_KEY = 16;
    int A_KEY = 1;
    int B_KEY = 2;
    int SELECT_KEY = 4;
    int START_KEY = 8;
    protected Utils utils = new Utils();
    protected BackgroundMapMod map = new BackgroundMapMod();
    int WHITE = 124;
    int LIGHT = 126;
    int BLACK = 127;
    int DARK = 125;
    protected boolean firstUpdate = true;

    void init() {
        drawEvents.init();
        this.asmInit();
        this.turnScreenOn();
    }

    int getBackgroundColor() {
        return this.map.getColor();
    }


    boolean updateScreen() {
        int cc;
        int xxx, yyy, indexxx;
        this.updateKeys();
        if (this.firstUpdate) {
            this.initDisplayRegisters();
            this.firstUpdate = false;
        }

        if (this.map.hasChanged()) {
            this.turnScreenOff();
            this.map.setStateUpdated();
            cc = map.getColor();
            this.copyColorIntoMap(cc);
            this.turnScreenOn();
        }

        this.drawEvents.drawList();
        return true;
    }
    void updateKeys() asm(
            "call UpdateKeys
            ret
            "
            );
    boolean isInVBlank() asm (
        "
        call UpdateKeys
        ld h, 0
        ld l, 0
        ld a, [rLY]
        cp 144
        jp c, notVBlank
        ld l, $ff
        ld h, $ff
        notVBlank:
        ret
        "
    );
    boolean notTooMuchVBlank() asm (
            "
    ld h, 0
    ld l, 0
    ld a, [rLY]
    cp 224
    jp nc, notTooMuchVBlank
    ld l, $ff
    ld h, $ff
    notTooMuchVBlank:
    ret
        "
                );
    void turnScreenOff() asm (
        "
        ;call WaitForOneVBlank
        ; Turn the LCD off
        ld a, 0
        ld [rLCDC], a
        ret
        "
    );
    void waitVBlank() asm (
            "
    call WaitForOneVBlank
            ret
        "
    );
    void waitVDraw() asm (
            "
    waitVDrawLoop:

    ld a, [rLY] ; Copy the vertical line to a
    or a, a; Check if the vertical line (in a) is 0
    jp z, waitVDrawLoop
    ret
        "
                );
    void turnScreenOn() asm (
        "
    ; Turn the LCD on
    ld a, LCDCF_ON | LCDCF_BGON | LCDCF_OBJON
    ld [rLCDC], a
    ret
        "
    );

    void sleep(int i) asm (
            "
            ld hl, sp + 4
            ld c, [hl]
            sleepLoop1:
                ld hl, $a3a; une centi seconde;
                sleepLoop2:
                    dec hl
                    ld a, l
                    or a, h
                    jp nz, sleepLoop2
                dec c
                ld a, c
                or a, a
                jp nz, sleepLoop1
            ret
            "
            );
    void initDisplayRegisters() asm (
        "
        ; During the first (blank) frame, initialize display registers
        ld a, %11100100
        ld [rBGP], a
        ret
        "
    );
    void setTile(int tileIndex, int x, int y) {
        this.drawEvents.add(tileIndex, x, y);
    }
    void setColor(int color, int x, int y) {
        this.setTile(color, x, y);
    }

    void printNumber(int n, int x, int y) {
        int d;
        int tileIndex = 88;
        if (n/10 != 0) {
            x = x + 1;
        }
        if (n/100 != 0) {
            x = x + 1;
        }
        if (n == 0) {
            utils.pushInTileMap(x, y, 88);
        }
        while(n != 0) {
            d = n%10;
            tileIndex = 88 + d;
            utils.pushInTileMap(x, y, tileIndex);
            n = n / 10;
            x = x - 1;
        }

    }

    //
    void setBackgroundColor(int color) {
        this.map.setColor(color);
    }
    void copyColorIntoMap(int c) {
        int index = c;
        this.utils.setBackGroundInTileMap(index);
    }

    void stop() asm (
            "
    stoppp:
            halt
            jp stoppp
        "
                );
    void asmInit () asm (
        "
        call WaitForOneVBlank

        ; Turn the LCD off
        ld a, 0
        ld [rLCDC], a


            ; init display reg
    ; During the first (blank) frame, initialize display registers
    ld a, %11100100
    ld [rBGP], a

    ; On met les tiles de l utilisateur en memoire
    ld de, Tiles
    ld hl, $9000; Ce seront les quatres dernières tiles
    ld bc, TilesEnd - Tiles
    call CopyDEintoMemoryAtHL

            ; On met les tiles elementaires dans la mémoire
    ld de, ElementaryTiles
    ld hl, $97c0; Ce seront les quatres dernières tiles
    ld bc, ElementaryTilesEnd - ElementaryTiles
    call CopyDEintoMemoryAtHL

    ld de, wLetters
    ld hl, $9580; Ce seront les quatres dernières tiles
    ld bc, wLettersEnd - wLetters
    call CopyDEintoMemoryAtHL

    ld b, 160
    ld hl, _OAMRAM

    ClearOAM:
    ld [hli], a
    dec b
    jp nz, ClearOAM

    call initVariables

            ; Turn the LCD on
    ld a, LCDCF_ON | LCDCF_BGON | LCDCF_OBJON
    ld [rLCDC], a

            ret


    ;ld hl, $97f0
        ret; comme ça on essaie pas d executer la suite

    SECTION \"Elementary Tile data\", ROM0
    ; Les tiles élémentaire
    ElementaryTiles:
    db $00,$00, $00,$00, $00,$00, $00,$00, $00,$00, $00,$00, $00,$00, $00,$00
    db $00,$ff, $00,$ff, $00,$ff, $00,$ff, $00,$ff, $00,$ff, $00,$ff, $00,$ff
    db $ff,$00, $ff,$00, $ff,$00, $ff,$00, $ff,$00, $ff,$00, $ff,$00, $ff,$00
    db $ff,$ff, $ff,$ff, $ff,$ff, $ff,$ff, $ff,$ff, $ff,$ff, $ff,$ff, $ff,$ff
    ElementaryTilesEnd:

    wLetters:
    db $38,$38,$44,$44,$4c,$4c,$54,$54,$64,$64,$44,$44,$38,$38,$00,$00 ; 0
    db $10,$10,$30,$30,$10,$10,$10,$10,$10,$10,$10,$10,$38,$38,$00,$00 ; 1
    db $38,$38,$44,$44,$04,$04,$08,$08,$10,$10,$20,$20,$7c,$7c,$00,$00 ; 2
    db $7c,$7c,$04,$04,$08,$08,$18,$18,$04,$04,$04,$04,$78,$78,$00,$00 ; 3
    db $0c,$0c,$14,$14,$24,$24,$44,$44,$7c,$7c,$04,$04,$04,$04,$00,$00 ; 4
    db $7c,$7c,$40,$40,$40,$40,$78,$78,$04,$04,$04,$04,$78,$78,$00,$00 ; 5
    db $18,$18,$20,$20,$40,$40,$78,$78,$44,$44,$44,$44,$38,$38,$00,$00 ; 6
    db $7c,$7c,$04,$04,$04,$04,$08,$08,$08,$08,$10,$10,$10,$10,$00,$00 ; 7
    db $38,$38,$44,$44,$44,$44,$38,$38,$44,$44,$44,$44,$38,$38,$00,$00 ; 8
    db $38,$38,$44,$44,$44,$44,$3c,$3c,$04,$04,$08,$08,$30,$30,$00,$00 ; 9
    db $00,$00,$38,$38,$04,$04,$3c,$3c,$44,$44,$44,$44,$3c,$3c,$00,$00 ; a
    db $40,$40,$58,$58,$64,$64,$44,$44,$44,$44,$44,$44,$78,$78,$00,$00 ; b
    db $00,$00,$38,$38,$44,$44,$40,$40,$40,$40,$44,$44,$38,$38,$00,$00 ; c
    db $04,$04,$34,$34,$4c,$4c,$44,$44,$44,$44,$44,$44,$3c,$3c,$00,$00 ; d
    db $00,$00,$38,$38,$44,$44,$7c,$7c,$40,$40,$44,$44,$38,$38,$00,$00 ; e
    db $0c,$0c,$10,$10,$3c,$3c,$10,$10,$10,$10,$10,$10,$10,$10,$10,$10 ; f
    db $00,$00,$3c,$3c,$44,$44,$44,$44,$44,$44,$3c,$3c,$04,$04,$38,$38 ; g
    db $40,$40,$58,$58,$64,$64,$44,$44,$44,$44,$44,$44,$44,$44,$00,$00 ; h
    db $00,$00,$30,$30,$10,$10,$10,$10,$10,$10,$10,$10,$10,$10,$00,$00 ; i
    db $00,$00,$18,$18,$08,$08,$08,$08,$08,$08,$08,$08,$08,$08,$30,$30 ; j
    db $20,$20,$24,$24,$28,$28,$30,$30,$28,$28,$24,$24,$22,$22,$00,$00 ; k
    db $30,$30,$10,$10,$10,$10,$10,$10,$10,$10,$10,$10,$10,$10,$00,$00 ; l
    db $00,$00,$fc,$fc,$92,$92,$92,$92,$92,$92,$92,$92,$92,$92,$00,$00 ; m
    db $00,$00,$78,$78,$44,$44,$44,$44,$44,$44,$44,$44,$44,$44,$00,$00 ; n
    db $00,$00,$38,$38,$44,$44,$44,$44,$44,$44,$44,$44,$38,$38,$00,$00 ; o
    db $00,$00,$78,$78,$44,$44,$44,$44,$44,$44,$64,$64,$58,$58,$40,$40 ; p
    db $00,$00,$3c,$3c,$44,$44,$44,$44,$44,$44,$4c,$4c,$34,$34,$04,$04 ; q
    db $00,$00,$2c,$2c,$30,$30,$20,$20,$20,$20,$20,$20,$20,$20,$00,$00 ; r
    db $00,$00,$3c,$3c,$40,$40,$38,$38,$04,$04,$04,$04,$78,$78,$00,$00 ; s
    db $10,$10,$3c,$3c,$10,$10,$10,$10,$10,$10,$10,$10,$0c,$0c,$00,$00 ; t
    db $00,$00,$44,$44,$44,$44,$44,$44,$44,$44,$44,$44,$3c,$3c,$00,$00 ; u
    db $00,$00,$44,$44,$44,$44,$28,$28,$28,$28,$10,$10,$10,$10,$00,$00 ; v
    db $00,$00,$91,$91,$91,$91,$4a,$4a,$5a,$5a,$24,$24,$24,$24,$00,$00 ; w
    db $00,$00,$44,$44,$28,$28,$10,$10,$10,$10,$28,$28,$44,$44,$00,$00 ; x
    db $00,$00,$44,$44,$44,$44,$28,$28,$28,$28,$10,$10,$10,$10,$60,$60 ; y
    db $00,$00,$3c,$3c,$04,$04,$08,$08,$10,$10,$20,$20,$3c,$3c,$00,$00 ; z
    wLettersEnd:

    SECTION \"Utils\", ROM0
    initVariables:
    ld a, 0
    ld [wCurKeys], a
    ld [wNewKeys], a
            ret
    CopyDEintoMemoryAtHL:
    ld a, [de]
    ld [hli], a
    inc de
    dec bc
    ld a, b
    or a, c
    jp nz, CopyDEintoMemoryAtHL ; Jump to COpyTiles, if the z flag is not set. (the last operation had a non zero result)
    ret;

    UpdateKeys:
    ; Poll half the controller
    ld a, P1F_GET_BTN
    call .onenibble
    ld b, a ; B7-4 = 1; B3-0 = unpressed buttons

            ; Poll the other half
    ld a, P1F_GET_DPAD
    call .onenibble
    swap a ; A3-0 = unpressed directions; A7-4 = 1
    xor a, b ; A = pressed buttons + directions
    ld b, a ; B = pressed buttons + directions

    ; And release the controller
    ld a, P1F_GET_NONE
    ldh [rP1], a

    ; Combine with previous wCurKeys to make wNewKeys
    ld a, [wCurKeys]
    xor a, b ; A = keys that changed state
    and a, b ; A = keys that changed to pressed
    ld l, a
    ld [wNewKeys], a
    ld a, b
    ld h, a
    ld [wCurKeys], a
            ret

  .onenibble
    ldh [rP1], a ; switch the key matrix
    call .knownret ; burn 10 cycles calling a known ret
    ldh a, [rP1] ; ignore value while waiting for the key matrix to settle
    ldh a, [rP1]
    ldh a, [rP1] ; this read counts
    or a, $F0 ; A7-4 = 1; A3-0 = unpressed keys
    ret
    .knownret
    ret

    rand:
    ; Add 0xB3 then multiply by 0x01010101
    ld hl, randstate+0
    ld a, [hl]
    add a, $B3
    ld [hl+], a
    adc a, [hl]
    ld [hl+], a
    adc a, [hl]
    ld [hl+], a
    ld c, a
    adc a, [hl]
    ld [hl], a
    ld b, a
    ret

    SECTION \"Variables\", WRAM0

    wVBlankCount: db
    wCurKeys: db
    wNewKeys: db
    randstate: ds 4


    SECTION \"VBlankFunctions\", ROM0

    WaitForOneVBlank::

    ; Wait a small amount of time
            ; Save our count in this variable
    ld a, 1
    ld [wVBlankCount], a

    WaitForVBlankFunction::

    WaitForVBlankFunction_Loop::

    ld a, [rLY] ; Copy the vertical line to a
    cp 144 ; Check if the vertical line (in a) is 0
    jp c, WaitForVBlankFunction_Loop ; A conditional jump. The condition is that 'c' is set, the last operation overflowed

    ld a, [wVBlankCount]
    sub a, 1
    ld [wVBlankCount], a
    ret z

    WaitForVBlankFunction_Loop2::

    ld a, [rLY] ; Copy the vertical line to a
    cp 144 ; Check if the vertical line (in a) is 0
    jp nc, WaitForVBlankFunction_Loop2 ; A conditional jump. The condition is that 'c' is set, the last operation overflowed

    jp WaitForVBlankFunction_Loop

            ; ANCHOR_END: vblank-utils
        "
    ); //
    boolean keyPressed(int pad) asm(
        "
        ;call UpdateKeys
        ;ld b, l; b = newKey;
        ld a, [wNewKeys]
        ld b, a
        ld hl, sp + 4
        ld a, [hl]; a = pad;

        ld hl, 0
        and a, b
        jp z ,notPressed
        ld hl, $ff
        notPressed:
        ret
        "
    );
}
