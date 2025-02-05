package at.ac.ase.inso.group02.views;

public class Views {
    public static class Brief {
    }

    public static class Full extends Brief {
    }

    public static class PublicBrief extends Brief {
    }

    public static class Public extends PublicBrief {
    }

    public static class Private extends Public {
    }

    public static class ImplicitlyTypedFull extends Full {
    }

    public static class ExplicitlyTypedFull extends ImplicitlyTypedFull {
    }

    public static class ExplicitlyTypedBrief extends Brief {
    }

    public static class ExplicitlyTypedWithoutUser {
    }
}