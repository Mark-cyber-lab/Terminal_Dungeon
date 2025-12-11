package elements.items;

public class Movables {
    // movable item name
    private final String name;

    // unique enemy identifier
    private final String id;

    public Movables( String name, String id ) {
        this.name = name;
        this.id = id;
    }

    public String getName() {return name;}

    public String getId() {return id;}
}
