# EditSign
###### &mdash; Edit signs by clicking them, it's as simple as it should be!

## Table of contents

1. [For server admins](#for-server-admins)
2. [For developers](#for-developers)
3. [Reporting a bug](#for-server-admins)

## For server admins
### Building from sources

1. Clone this repository

```
git clone https://github.com/benjaminbrassart/EditSign.git
```

2. Navigate in the freshly cloned directory

```
cd EditSign
```

3. Compiling

#### On Linux/UNIX

```
./gradlew jar
```

#### On Windows

```
gradlew.bat jar
```

### Installing

Once you built the jar from sources or grabbed a release,
just drop it in your Bukkit server's `plugins` directory.

### Permissions

`editsign.edit` - Allow a player to click to edit a sign

`editsign.color` - Allow a player to use colors in a sign

`editsign.place-copied` - Allow a player to place a cloned sign

## For developers
### Forking / pull requests

You're very welcome to fork this repository!

Pull requests fixing bugs or bringing new features are also welcome.

### Using as an API

You can use SignEdit as an API. Here are the main things you may want
to use:

#### Adding support for a specific version

You will have to extend the class `EditSignUtils` in order to support
a specific version of Bukkit.

```java
public class EditSignUtilsX extends EditSignUtils {
    
    @Override
    public void openSign(HumanEntity player, Sign sign) {
        // Implement...
    }
}
```

Once you implemented your code, you will have to register your class
for a version number.

```java
EditSign.registerVersion(version, supplier);
```

Where `version` is an `int` and `supplier` is a `Supplier<? extends EditSignUtils>`.

For example, to register Bukkit 1.7:

```java
static {
    EditSign.registerVersion(7, EditSignUtils7::new);
}
```

(For more information, check the classes in `fr.bbrassart.util`)

If you register a version, it **will** override the one that is already implemented
for this version.

## Reporting a bug

To report a bug, please follow this protocol:

1. Make sure it has not been reported yet
2. Open an issue on this repository
3. Give it a concise title (i.e. `Plugin not loading on Bukkit 1.6.2`)
4. Describe your issue, including:
    + The version of EditSign in use
    + The version of Bukkit in use
    + The complete list of plugins in use 
    + The steps to reproduce
    + The full stacktrace of the error