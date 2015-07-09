package stream.vis.data;

public interface Schema {
    public boolean isStream();
    public int getNumAttrs ();
    public int getAttrType (int pos);
    public int getAttrLen (int pos);
}
