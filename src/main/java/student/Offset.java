package student;

public class Offset {
    private int rankOffset;
    private int fileOffset;

    public Offset(int rankOffset, int fileOffset) {
        this.rankOffset = rankOffset;
        this.fileOffset = fileOffset;
    }

    public int rankOffset(){
        return this.rankOffset;
    }

    public int fileOffset(){
        return this.fileOffset;
    }
}
