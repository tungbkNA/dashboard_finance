<template>
  <div class="view-container p-4">
    <h1 class="text-2xl font-semibold mb-4">Cài Đặt Dự Án</h1>

    <TabView>
      <TabPanel>
        <template #header>
          <span><i class="pi pi-folder mr-2" />Dự án</span>
        </template>
        <ProjectTab />
      </TabPanel>

      <TabPanel>
        <template #header>
          <span><i class="pi pi-tags mr-2" />Loại dự án</span>
        </template>
        <ProjectTypeTab />
      </TabPanel>

      <TabPanel>
        <template #header>
          <span><i class="pi pi-users mr-2" />Khách hàng</span>
        </template>
        <CustomerTab />
      </TabPanel>
    </TabView>
  </div>
</template>

<script setup lang="ts">
import TabView from 'primevue/tabview'
import TabPanel from 'primevue/tabpanel'
import ProjectTab from '@/components/project-settings/ProjectTab.vue'
import ProjectTypeTab from '@/components/project-settings/ProjectTypeTab.vue'
import CustomerTab from '@/components/project-settings/CustomerTab.vue'
</script>
