<template>
  <div class="carousel">
    <div class="slides">
      <div 
        v-for="slide in slides" 
        :key="slide.id"
        class="slide"
        :class="{ active: currentSlide === slide.id }"
      >
        <img :src="slide.image" :alt="slide.title">
        <div class="slide-info">
          <h3>{{ slide.title }}</h3>
          <p>{{ slide.description }}</p>
        </div>
      </div>
    </div>
    
    <div class="controls">
      <button @click="prevSlide">❮</button>
      <button @click="nextSlide">❯</button>
    </div>
    
    <div class="indicators">
      <span 
        v-for="slide in slides" 
        :key="slide.id"
        :class="{ active: currentSlide === slide.id }"
        @click="goToSlide(slide.id)"
      ></span>
    </div>
  </div>
</template>

<script>
import { ref, onMounted } from 'vue'

export default {
  props: {
    slides: {
      type: Array,
      required: true
    }
  },
  setup(props) {
    const currentSlide = ref(props.slides[0].id)
    let interval
    
    const nextSlide = () => {
      const currentIndex = props.slides.findIndex(s => s.id === currentSlide.value)
      const nextIndex = (currentIndex + 1) % props.slides.length
      currentSlide.value = props.slides[nextIndex].id
    }
    
    const prevSlide = () => {
      const currentIndex = props.slides.findIndex(s => s.id === currentSlide.value)
      const prevIndex = (currentIndex - 1 + props.slides.length) % props.slides.length
      currentSlide.value = props.slides[prevIndex].id
    }
    
    const goToSlide = (id) => {
      currentSlide.value = id
    }
    
    onMounted(() => {
      interval = setInterval(nextSlide, 5000)
    })
    
    return {
      currentSlide,
      nextSlide,
      prevSlide,
      goToSlide
    }
  }
}
</script>

<style scoped>
.carousel {
  position: relative;
  overflow: hidden;
  height: 400px;
  border-radius: 8px;
  box-shadow: 0 4px 8px rgba(0,0,0,0.2);
}

.slides {
  position: relative;
  height: 100%;
}

.slide {
  position: absolute;
  top: 0;
  left: 0;
  width: 100%;
  height: 100%;
  opacity: 0;
  transition: opacity 1s ease-in-out;
}

.slide.active {
  opacity: 1;
}

.slide img {
  width: 100%;
  height: 100%;
  object-fit: cover;
}

.slide-info {
  position: absolute;
  bottom: 0;
  left: 0;
  right: 0;
  background: rgba(0,0,0,0.6);
  color: white;
  padding: 20px;
}

.slide-info h3 {
  margin: 0 0 10px 0;
}

.controls {
  position: absolute;
  top: 50%;
  width: 100%;
  display: flex;
  justify-content: space-between;
  transform: translateY(-50%);
}

.controls button {
  background: rgba(255,255,255,0.5);
  border: none;
  padding: 10px 15px;
  font-size: 18px;
  cursor: pointer;
  color: #333;
  border-radius: 50%;
  margin: 0 20px;
}

.indicators {
  position: absolute;
  bottom: 20px;
  left: 50%;
  transform: translateX(-50%);
  display: flex;
}

.indicators span {
  width: 12px;
  height: 12px;
  margin: 0 5px;
  background-color: rgba(255,255,255,0.5);
  border-radius: 50%;
  cursor: pointer;
}

.indicators span.active {
  background-color: white;
}
</style>