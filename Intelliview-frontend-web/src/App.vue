<template>
  <div class="app-shell">
    <router-view v-slot="{ Component }">
      <transition name="fade" mode="out-in">
        <component :is="Component" />
      </transition>
    </router-view>
  </div>
</template>

<script setup lang="ts">
// Root app shell
</script>

<style>
:root {
  color-scheme: light;
  --color-primary: #2f6fed;
  --color-primary-strong: #1f53b8;
  --color-primary-soft: rgba(47, 111, 237, 0.1);
  --color-accent: #18a999;
  --color-accent-soft: rgba(24, 169, 153, 0.12);
  --color-warm: #d9822b;
  --color-warm-soft: rgba(240, 138, 36, 0.14);
  --color-success: #19a97b;
  --color-danger: #d64545;
  --color-bg: #f4f7fb;
  --color-bg-elevated: rgba(255, 255, 255, 0.92);
  --color-bg-panel: #ffffff;
  --color-bg-muted: #ebf1ff;
  --color-bg-strong: #dfe9ff;
  --color-border: rgba(138, 155, 188, 0.24);
  --color-border-strong: rgba(68, 92, 134, 0.26);
  --color-text: #18233a;
  --color-text-secondary: #4c5d7a;
  --color-text-muted: #7d8ca7;
  --shadow-card: 0 14px 34px rgba(29, 45, 82, 0.07);
  --shadow-soft: 0 8px 20px rgba(47, 111, 237, 0.1);
  --shadow-strong: 0 18px 36px rgba(31, 83, 184, 0.16);
  --radius-xl: 8px;
  --radius-lg: 8px;
  --radius-md: 8px;
  --radius-sm: 6px;
}

html {
  scroll-behavior: smooth;
}

html.dark-mode {
  color-scheme: dark;
  --color-primary: #7da2ff;
  --color-primary-strong: #a8c0ff;
  --color-primary-soft: rgba(125, 162, 255, 0.18);
  --color-accent: #52d4c7;
  --color-accent-soft: rgba(82, 212, 199, 0.16);
  --color-warm: #ffb45b;
  --color-warm-soft: rgba(255, 180, 91, 0.14);
  --color-success: #57d89f;
  --color-danger: #ff7b7b;
  --color-bg: #0f1728;
  --color-bg-elevated: rgba(17, 27, 46, 0.88);
  --color-bg-panel: #16233b;
  --color-bg-muted: #1d2e4d;
  --color-bg-strong: #21365a;
  --color-border: rgba(136, 164, 215, 0.18);
  --color-border-strong: rgba(168, 191, 235, 0.26);
  --color-text: #f2f6ff;
  --color-text-secondary: #c0cbe0;
  --color-text-muted: #8ea0c3;
  --shadow-card: 0 24px 56px rgba(0, 0, 0, 0.24);
  --shadow-soft: 0 12px 28px rgba(0, 0, 0, 0.2);
  --shadow-strong: 0 24px 48px rgba(0, 0, 0, 0.3);
}

* {
  margin: 0;
  padding: 0;
  box-sizing: border-box;
}

body {
  font-family: "Avenir Next", "PingFang SC", "Microsoft YaHei", "Helvetica Neue", sans-serif;
  background: var(--color-bg);
  color: var(--color-text);
  transition: background 0.28s ease, color 0.28s ease;
}

a {
  color: inherit;
}

button,
input,
select,
textarea {
  font: inherit;
}

#app,
.app-shell {
  min-height: 100vh;
}

.fade-enter-active,
.fade-leave-active {
  transition: opacity 0.24s ease, transform 0.24s ease;
}

.fade-enter-from,
.fade-leave-to {
  opacity: 0;
  transform: translateY(8px);
}
</style>
