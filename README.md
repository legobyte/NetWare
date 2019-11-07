# Netware
**Neware** is an android library to watch for `network connectivity changes` written in `kotlin` with the power of `LiveData`
`kotlin extensions` `kotlin infix functions`

## Usage
**1. LifeCycle Based**
````kotlin
Netware.getInstance(context = this).observe(lifecycleOwner = this, observer = Observer { event ->
  // do something with event
})
// or
netware with this observe {event->
  // do something with event
}
````
**2. ViewBased**
````kotlin
netware with myView observe {event->
  // do something with event
}
````
**3. Observe forever**
in this sample you should unregister the observer by yourself otherwise **MemoryLeak** will occur
````kotlin
val registry = Netware.getInstance(context = this).observeForever(Observer {

})
// or
val registry = netware observeForever{event ->

}
// call this method when your done with Netware
registry.unregister()
````

## Setup
In your ProjectLevel `build.gradle`
````gradle
allprojects {
    repositories {
        jcenter()
    }
}
````
In your AppLevel `build.gradle`
````gradle
dependencies {
    implementation 'org.legobyte:netware:1.0.1'
}
````

## License

    Copyright 2019 LegoBytes

    Licensed under the Apache License, Version 2.0 (the "License");
    you may not use this file except in compliance with the License.
    You may obtain a copy of the License at

       http://www.apache.org/licenses/LICENSE-2.0

    Unless required by applicable law or agreed to in writing, software
    distributed under the License is distributed on an "AS IS" BASIS,
    WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
    See the License for the specific language governing permissions and
    limitations under the License.