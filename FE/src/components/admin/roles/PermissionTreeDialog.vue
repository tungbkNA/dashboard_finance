<template>
  <Dialog
    v-model:visible="visible"
    :header="`Phân quyền: ${roleName}`"
    modal
    :style="{ width: '560px' }"
    @hide="$emit('close')"
  >
    <div v-if="loadingPerms" class="flex justify-center py-4">
      <ProgressSpinner style="width: 40px; height: 40px" />
    </div>
    <div v-else>
      <Tree
        v-model:selectionKeys="selectedKeys"
        :value="treeNodes"
        selectionMode="checkbox"
        class="w-full"
      />
    </div>

    <template #footer>
      <Button label="Hủy" severity="secondary" text @click="$emit('close')" />
      <Button label="Lưu phân quyền" icon="pi pi-save" :loading="saving" @click="handleSave" />
    </template>
  </Dialog>
</template>

<script setup lang="ts">
import { ref, onMounted } from 'vue'
import Dialog from 'primevue/dialog'
import Tree from 'primevue/tree'
import Button from 'primevue/button'
import ProgressSpinner from 'primevue/progressspinner'
import type { Permission } from '@/types/role'
import { roleService } from '@/services/roleService'

const props = defineProps<{ roleId: string; roleName: string }>()
const emit = defineEmits<{ saved: []; close: [] }>()

const visible = ref(true)
const loadingPerms = ref(false)
const saving = ref(false)
const treeNodes = ref<unknown[]>([])
const selectedKeys = ref<Record<string, { checked: boolean; partialChecked: boolean }>>({})

onMounted(async () => {
  loadingPerms.value = true
  try {
    const [allPerms, roleDetail] = await Promise.all([
      roleService.listPermissions(),
      roleService.getRolePermissions(props.roleId)
    ])
    treeNodes.value = buildTree(allPerms)
    // Pre-select current permissions
    const currentCodes = new Set(roleDetail.permissions.map((p) => p.code))
    for (const code of currentCodes) {
      selectedKeys.value[code] = { checked: true, partialChecked: false }
    }
    // Mark parent nodes as partialChecked if needed
    for (const node of allPerms.filter((p) => !p.parentCode)) {
      const children = allPerms.filter((p) => p.parentCode === node.code)
      if (children.length > 0) {
        const checkedCount = children.filter((c) => currentCodes.has(c.code)).length
        if (checkedCount > 0 && checkedCount < children.length) {
          selectedKeys.value[node.code] = { checked: false, partialChecked: true }
        } else if (checkedCount === children.length) {
          selectedKeys.value[node.code] = { checked: true, partialChecked: false }
        }
      }
    }
  } finally {
    loadingPerms.value = false
  }
})

function buildTree(permissions: Permission[]) {
  const roots = permissions.filter((p) => !p.parentCode)
  return roots.map((root) => ({
    key: root.code,
    label: root.displayName,
    children: permissions
      .filter((p) => p.parentCode === root.code)
      .map((child) => ({ key: child.code, label: child.displayName }))
  }))
}

async function handleSave() {
  saving.value = true
  try {
    const codes = Object.entries(selectedKeys.value)
      .filter(([, v]) => v.checked)
      .map(([k]) => k)
    await roleService.updateRolePermissions(props.roleId, { permissions: codes })
    emit('saved')
  } finally {
    saving.value = false
  }
}
</script>
