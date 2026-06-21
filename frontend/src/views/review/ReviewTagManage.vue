<template>
  <div class="review-tag-container">
    <el-card shadow="never" class="search-card">
      <div class="search-row">
        <div style="flex: 1" />
        <el-button v-if="hasPermission('review:tag:query')" type="success" plain @click="handlePreview">
          <el-icon><View /></el-icon>预览
        </el-button>
        <el-button
          v-if="canAddCurrent"
          type="primary"
          @click="handleAddCurrent"
        >
          <el-icon><Plus /></el-icon>{{ currentTab === 'comment' ? '添加评语' : '添加标签' }}
        </el-button>
      </div>
    </el-card>

    <el-card shadow="never" class="table-card">
      <el-tabs v-model="currentTab" @tab-change="handleTabChange">
        <el-tab-pane label="好评标签" name="good">
          <div class="tag-grid">
            <div
              v-for="(tag, index) in goodTags"
              :key="tag.id"
              class="tag-card good-tag"
            >
              <span class="tag-text">{{ tag.tagText }}</span>
              <div class="tag-actions" v-if="hasTagEditPermission">
                <el-button
                  v-if="hasPermission('review:tag:edit')"
                  type="primary"
                  link
                  size="small"
                  @click="handleEditTag(tag)"
                >编辑</el-button>
                <el-button
                  v-if="index > 0 && hasPermission('review:tag:edit')"
                  link
                  size="small"
                  @click="handleMoveTag('good', index, -1)"
                >
                  <el-icon><Top /></el-icon>
                </el-button>
                <el-button
                  v-if="index < goodTags.length - 1 && hasPermission('review:tag:edit')"
                  link
                  size="small"
                  @click="handleMoveTag('good', index, 1)"
                >
                  <el-icon><Bottom /></el-icon>
                </el-button>
                <el-button
                  v-if="hasPermission('review:tag:delete')"
                  type="danger"
                  link
                  size="small"
                  @click="handleDeleteTag(tag)"
                >删除</el-button>
              </div>
            </div>
            <el-empty v-if="goodTags.length === 0" description="暂无好评标签" />
          </div>
        </el-tab-pane>

        <el-tab-pane label="差评标签" name="bad">
          <div class="tag-grid">
            <div
              v-for="(tag, index) in badTags"
              :key="tag.id"
              class="tag-card bad-tag"
            >
              <span class="tag-text">{{ tag.tagText }}</span>
              <div class="tag-actions" v-if="hasTagEditPermission">
                <el-button
                  v-if="hasPermission('review:tag:edit')"
                  type="primary"
                  link
                  size="small"
                  @click="handleEditTag(tag)"
                >编辑</el-button>
                <el-button
                  v-if="index > 0 && hasPermission('review:tag:edit')"
                  link
                  size="small"
                  @click="handleMoveTag('bad', index, -1)"
                >
                  <el-icon><Top /></el-icon>
                </el-button>
                <el-button
                  v-if="index < badTags.length - 1 && hasPermission('review:tag:edit')"
                  link
                  size="small"
                  @click="handleMoveTag('bad', index, 1)"
                >
                  <el-icon><Bottom /></el-icon>
                </el-button>
                <el-button
                  v-if="hasPermission('review:tag:delete')"
                  type="danger"
                  link
                  size="small"
                  @click="handleDeleteTag(tag)"
                >删除</el-button>
              </div>
            </div>
            <el-empty v-if="badTags.length === 0" description="暂无差评标签" />
          </div>
        </el-tab-pane>

        <el-tab-pane label="快捷评语" name="comment">
          <el-table :data="commentList" stripe border style="width: 100%">
            <el-table-column label="评语内容" min-width="400" show-overflow-tooltip>
              <template #default="{ row }">
                <span>{{ truncateText(row.commentContent, 30) }}</span>
              </template>
            </el-table-column>
            <el-table-column label="分类" width="120" align="center">
              <template #default="{ row }">
                <el-tag
                  :type="row.commentType === 1 ? 'success' : row.commentType === 2 ? 'warning' : 'danger'"
                  size="small"
                >
                  {{ commentTypeMap[row.commentType] }}
                </el-tag>
              </template>
            </el-table-column>
            <el-table-column prop="createTime" label="创建时间" width="180" align="center" />
            <el-table-column label="操作" width="160" align="center" fixed="right">
              <template #default="{ row }">
                <el-button
                  v-if="hasPermission('review:comment:edit')"
                  type="primary"
                  link
                  size="small"
                  @click="handleEditComment(row)"
                >编辑</el-button>
                <el-button
                  v-if="hasPermission('review:comment:delete')"
                  type="danger"
                  link
                  size="small"
                  @click="handleDeleteComment(row)"
                >删除</el-button>
              </template>
            </el-table-column>
          </el-table>
          <el-empty v-if="commentList.length === 0" description="暂无快捷评语" />
        </el-tab-pane>
      </el-tabs>
    </el-card>

    <el-dialog
      v-model="tagDialogVisible"
      :title="tagDialogForm.id ? '编辑标签' : '添加标签'"
      width="420px"
      destroy-on-close
    >
      <el-form ref="tagFormRef" :model="tagDialogForm" :rules="tagFormRules" label-width="80px">
        <el-form-item label="标签文本" prop="tagText">
          <el-input
            v-model="tagDialogForm.tagText"
            placeholder="请输入标签文本（2-10个字）"
            maxlength="10"
            show-word-limit
          />
        </el-form-item>
      </el-form>
      <template #footer>
        <el-button @click="tagDialogVisible = false">取消</el-button>
        <el-button type="primary" :loading="tagDialogSaving" @click="handleTagSubmit">保存</el-button>
      </template>
    </el-dialog>

    <el-dialog
      v-model="commentDialogVisible"
      :title="commentDialogForm.id ? '编辑快捷评语' : '添加快捷评语'"
      width="520px"
      destroy-on-close
    >
      <el-form ref="commentFormRef" :model="commentDialogForm" :rules="commentFormRules" label-width="80px">
        <el-form-item label="评语内容" prop="commentContent">
          <el-input
            v-model="commentDialogForm.commentContent"
            type="textarea"
            :rows="4"
            placeholder="请输入评语内容（5-100个字）"
            maxlength="100"
            show-word-limit
          />
        </el-form-item>
        <el-form-item label="分类" prop="commentType">
          <el-select v-model="commentDialogForm.commentType" placeholder="请选择分类" style="width: 100%">
            <el-option label="好评评语" :value="1" />
            <el-option label="中性评语" :value="2" />
            <el-option label="差评评语" :value="3" />
          </el-select>
        </el-form-item>
      </el-form>
      <template #footer>
        <el-button @click="commentDialogVisible = false">取消</el-button>
        <el-button type="primary" :loading="commentDialogSaving" @click="handleCommentSubmit">保存</el-button>
      </template>
    </el-dialog>

    <el-dialog v-model="previewVisible" title="评价标签预览" width="600px" destroy-on-close>
      <div class="preview-container">
        <div class="preview-section">
          <h4 class="preview-section-title">好评标签</h4>
          <div class="preview-tag-grid">
            <span
              v-for="tag in goodTags"
              :key="tag.id"
              :class="['preview-tag', 'preview-good-tag', { 'selected': previewSelectedGood.has(tag.id) }]"
              @click="togglePreviewTag('good', tag.id)"
            >{{ tag.tagText }}</span>
          </div>
          <el-empty v-if="goodTags.length === 0" :image-size="60" description="暂无好评标签" />
        </div>
        <div class="preview-section">
          <h4 class="preview-section-title">差评标签</h4>
          <div class="preview-tag-grid">
            <span
              v-for="tag in badTags"
              :key="tag.id"
              :class="['preview-tag', 'preview-bad-tag', { 'selected': previewSelectedBad.has(tag.id) }]"
              @click="togglePreviewTag('bad', tag.id)"
            >{{ tag.tagText }}</span>
          </div>
          <el-empty v-if="badTags.length === 0" :image-size="60" description="暂无差评标签" />
        </div>
        <div class="preview-tip">
          <el-icon><InfoFilled /></el-icon>
          点击标签可模拟选中效果，客人可多选标签
        </div>
      </div>
    </el-dialog>
  </div>
</template>

<script setup>
import { ref, reactive, computed, onMounted } from 'vue'
import { ElMessage, ElMessageBox } from 'element-plus'
import { Plus, View, Top, Bottom, InfoFilled } from '@element-plus/icons-vue'
import { useUserStore } from '@/stores/user'
import api from '@/api'

const userStore = useUserStore()
const hasPermission = (p) => userStore.hasPermission(p)

const currentTab = ref('good')
const goodTags = ref([])
const badTags = ref([])
const commentList = ref([])

const commentTypeMap = {
  1: '好评评语',
  2: '中性评语',
  3: '差评评语'
}

const canAddCurrent = computed(() => {
  if (currentTab.value === 'comment') {
    return hasPermission('review:comment:add')
  }
  return hasPermission('review:tag:add')
})

const hasTagEditPermission = computed(() => {
  return hasPermission('review:tag:edit') || hasPermission('review:tag:delete')
})

const tagDialogVisible = ref(false)
const tagDialogSaving = ref(false)
const tagFormRef = ref(null)
const tagDialogForm = reactive({
  id: null,
  tagType: 1,
  tagText: ''
})

const tagFormRules = {
  tagText: [
    { required: true, message: '请输入标签文本', trigger: 'blur' },
    { min: 2, max: 10, message: '标签长度必须在2-10个字之间', trigger: 'blur' }
  ]
}

const commentDialogVisible = ref(false)
const commentDialogSaving = ref(false)
const commentFormRef = ref(null)
const commentDialogForm = reactive({
  id: null,
  commentContent: '',
  commentType: 1
})

const commentFormRules = {
  commentContent: [
    { required: true, message: '请输入评语内容', trigger: 'blur' },
    { min: 5, max: 100, message: '评语长度必须在5-100个字之间', trigger: 'blur' }
  ],
  commentType: [{ required: true, message: '请选择分类', trigger: 'change' }]
}

const previewVisible = ref(false)
const previewSelectedGood = reactive(new Set())
const previewSelectedBad = reactive(new Set())

const truncateText = (text, max) => {
  if (!text) return ''
  return text.length > max ? text.substring(0, max) + '...' : text
}

const loadGoodTags = async () => {
  try {
    const res = await api.reviewTag.list({ tagType: 1 })
    if (res.code === 200) {
      goodTags.value = res.data || []
    }
  } catch {
    goodTags.value = []
  }
}

const loadBadTags = async () => {
  try {
    const res = await api.reviewTag.list({ tagType: 2 })
    if (res.code === 200) {
      badTags.value = res.data || []
    }
  } catch {
    badTags.value = []
  }
}

const loadComments = async () => {
  try {
    const res = await api.reviewQuickComment.list()
    if (res.code === 200) {
      commentList.value = res.data || []
    }
  } catch {
    commentList.value = []
  }
}

const handleTabChange = () => {
}

const handleAddCurrent = () => {
  if (currentTab.value === 'comment') {
    commentDialogForm.id = null
    commentDialogForm.commentContent = ''
    commentDialogForm.commentType = 1
    commentDialogVisible.value = true
  } else {
    tagDialogForm.id = null
    tagDialogForm.tagType = currentTab.value === 'good' ? 1 : 2
    tagDialogForm.tagText = ''
    tagDialogVisible.value = true
  }
}

const handleEditTag = (row) => {
  tagDialogForm.id = row.id
  tagDialogForm.tagType = row.tagType
  tagDialogForm.tagText = row.tagText
  tagDialogVisible.value = true
}

const handleTagSubmit = async () => {
  const valid = await tagFormRef.value.validate().catch(() => false)
  if (!valid) return
  tagDialogSaving.value = true
  try {
    const payload = {
      tagType: tagDialogForm.tagType,
      tagText: tagDialogForm.tagText
    }
    const res = tagDialogForm.id
      ? await api.reviewTag.update({ ...payload, id: tagDialogForm.id })
      : await api.reviewTag.add(payload)
    if (res.code === 200) {
      ElMessage.success(tagDialogForm.id ? '编辑成功' : '添加成功')
      tagDialogVisible.value = false
      loadGoodTags()
      loadBadTags()
    } else {
      ElMessage.error(res.message || '操作失败')
    }
  } catch {
    ElMessage.error('操作失败')
  } finally {
    tagDialogSaving.value = false
  }
}

const handleDeleteTag = (row) => {
  ElMessageBox.confirm('确定删除该标签？', '提示', {
    confirmButtonText: '确定',
    cancelButtonText: '取消',
    type: 'warning'
  }).then(async () => {
    try {
      const res = await api.reviewTag.delete(row.id)
      if (res.code === 200) {
        ElMessage.success('删除成功')
        loadGoodTags()
        loadBadTags()
      } else {
        ElMessage.error(res.message || '删除失败')
      }
    } catch {
      ElMessage.error('删除失败')
    }
  }).catch(() => {})
}

const handleMoveTag = async (type, index, direction) => {
  const list = type === 'good' ? goodTags.value : badTags.value
  const newIndex = index + direction
  if (newIndex < 0 || newIndex >= list.length) return

  const temp = list[index]
  list[index] = list[newIndex]
  list[newIndex] = temp

  const ids = list.map(t => t.id)
  try {
    await api.reviewTag.updateSort(ids)
    if (type === 'good') loadGoodTags()
    else loadBadTags()
  } catch {
    ElMessage.error('排序失败')
  }
}

const handleEditComment = (row) => {
  commentDialogForm.id = row.id
  commentDialogForm.commentContent = row.commentContent
  commentDialogForm.commentType = row.commentType
  commentDialogVisible.value = true
}

const handleCommentSubmit = async () => {
  const valid = await commentFormRef.value.validate().catch(() => false)
  if (!valid) return
  commentDialogSaving.value = true
  try {
    const payload = {
      commentContent: commentDialogForm.commentContent,
      commentType: commentDialogForm.commentType
    }
    const res = commentDialogForm.id
      ? await api.reviewQuickComment.update({ ...payload, id: commentDialogForm.id })
      : await api.reviewQuickComment.add(payload)
    if (res.code === 200) {
      ElMessage.success(commentDialogForm.id ? '编辑成功' : '添加成功')
      commentDialogVisible.value = false
      loadComments()
    } else {
      ElMessage.error(res.message || '操作失败')
    }
  } catch {
    ElMessage.error('操作失败')
  } finally {
    commentDialogSaving.value = false
  }
}

const handleDeleteComment = (row) => {
  ElMessageBox.confirm('确定删除该快捷评语？', '提示', {
    confirmButtonText: '确定',
    cancelButtonText: '取消',
    type: 'warning'
  }).then(async () => {
    try {
      const res = await api.reviewQuickComment.delete(row.id)
      if (res.code === 200) {
        ElMessage.success('删除成功')
        loadComments()
      } else {
        ElMessage.error(res.message || '删除失败')
      }
    } catch {
      ElMessage.error('删除失败')
    }
  }).catch(() => {})
}

const handlePreview = () => {
  previewSelectedGood.clear()
  previewSelectedBad.clear()
  previewVisible.value = true
}

const togglePreviewTag = (type, id) => {
  const set = type === 'good' ? previewSelectedGood : previewSelectedBad
  if (set.has(id)) {
    set.delete(id)
  } else {
    set.add(id)
  }
}

onMounted(() => {
  loadGoodTags()
  loadBadTags()
  loadComments()
})
</script>

<style scoped>
.review-tag-container {
  padding: 10px;
}

.search-card {
  border-radius: 12px;
  border: none;
  margin-bottom: 12px;
}

.search-row {
  display: flex;
  align-items: center;
  gap: 10px;
}

.table-card {
  border-radius: 12px;
  border: none;
}

.tag-grid {
  display: flex;
  flex-wrap: wrap;
  gap: 12px;
  padding: 8px 0;
}

.tag-card {
  display: inline-flex;
  align-items: center;
  gap: 8px;
  padding: 10px 16px;
  border-radius: 20px;
  font-size: 14px;
  transition: all 0.2s;
  border: 1px solid transparent;
}

.good-tag {
  background: #f0f9eb;
  color: #67c23a;
  border-color: #e1f3d8;
}

.bad-tag {
  background: #fef0f0;
  color: #f56c6c;
  border-color: #fde2e2;
}

.tag-text {
  font-weight: 500;
}

.tag-actions {
  display: inline-flex;
  align-items: center;
  gap: 2px;
  margin-left: 4px;
  padding-left: 8px;
  border-left: 1px solid rgba(0, 0, 0, 0.1);
}

.preview-container {
  padding: 10px;
}

.preview-section {
  margin-bottom: 20px;
}

.preview-section:last-child {
  margin-bottom: 0;
}

.preview-section-title {
  margin: 0 0 12px;
  font-size: 14px;
  color: #303133;
  font-weight: 500;
}

.preview-tag-grid {
  display: flex;
  flex-wrap: wrap;
  gap: 10px;
}

.preview-tag {
  display: inline-block;
  padding: 8px 16px;
  border-radius: 16px;
  font-size: 13px;
  cursor: pointer;
  transition: all 0.2s;
  user-select: none;
  border: 1px solid transparent;
}

.preview-good-tag {
  background: #f0f9eb;
  color: #67c23a;
  border-color: #e1f3d8;
}

.preview-good-tag.selected {
  background: #67c23a;
  color: #fff;
  border-color: #67c23a;
}

.preview-bad-tag {
  background: #fef0f0;
  color: #f56c6c;
  border-color: #fde2e2;
}

.preview-bad-tag.selected {
  background: #f56c6c;
  color: #fff;
  border-color: #f56c6c;
}

.preview-tip {
  margin-top: 16px;
  padding: 10px;
  background: #f5f7fa;
  border-radius: 6px;
  font-size: 12px;
  color: #909399;
  display: flex;
  align-items: center;
  gap: 4px;
}
</style>
