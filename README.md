# Overview
This is my implementation of a ray tracer for graphics class. I had implemented a more primitive ray tracer from scratch as a personal project prior to taking the class. It uses Java 13.

## Features
### Cosine Similarity Weighted Hemisphere Sampling
This feature creates a more realistic image by giving less weight to light rays hitting our objects at high angles of incidence. This is done without ray-tracing.
![Cosine Similarity Weighted Hemisphere Sampling Figure](/cwhs.jpg)
#### Instructions
```ant run -Dargs=scene/cbox-global.xml```

### Direct Path Tracing
This is our primitive ray-tracer. Notice how shadows are completely dark without ray bounces.
![Direct Path Tracing Figure](/direct-tracer.png) 
#### Instructions
```ant run -Dargs=scene/cbox-direct.xml```

### Russian Roulette Recursive Path Tracing With Ray Bounces
This feature allows for a formidable trade-off between computational efficiency and accuracy. Ray bounces significantly improve image quality.
![Russian Roulette Recursive Path Tracing With Ray Bounces Figure](/rr-tracer.png) 
#### Instructions
```ant run -Dargs=scene/cbox-global.xml```

### Rough and Textured Solar System
Created a disproportionate system with textured planets of various shapes with rough skin perturbations.
![Rough and Textured Solar System Figure](/rr-tracer.png)
#### Instructions
```ant run -Dargs=scene/cbox-texture-submit.xml```