<template>
  <div class="review-display-container">
    <el-card shadow="never" class="stats-card">
      <el-row :gutter="20">
        <el-col :span="8">
          <div class="stat-item total-count">
            <div class="stat-icon">
              <el-icon><ChatDotRound /></el-icon>
            </div>
            <div class="stat-info">
              <div class="stat-label">评价总数</div>
              <div class="stat-value">{{ statistics.totalCount || 0 }}</div>
              <div class="stat-unit">条</div>
            </div>
          </div>
        </el-col>
        <el-col :span="8">
          <div class="stat-item avg-score">
            <div class="stat-icon">
              <el-icon><Star /></el-icon>
            </div>
            <div class="stat-info">
              <div class="stat-label">综合评分</div>
              <div class="stat-value">{{ statistics.avgScore || '0.0' }}</div>
              <div class="stat-unit">分</div>
            </div>
          </div>
        </el-col>
        <el-col :span="8">
          <div class="stat-item good-rate">
            <div class="stat-icon">
              <el-icon><CircleCheckFilled /></el-icon>
            </div>
            <div class="stat-info">
              <div class="stat-label">好评率</div>
              <div class="stat-value">{{ statistics.goodRate || 0 }}</div>
              <div class="stat-unit">%</div>
            </div>
          </div>
        </el-col>
      </el-row>
    </el-card>

    <el-row :gutter="20">
      <el-col :span="8">
        <el-card shadow="never" class="distribution-card">
          <div class="card-title">评分分布</div>
          <div class="distribution-list">
            <div
              v-for="item in statistics.scoreDistribution || []"
              :key="item.score"
              class="distribution-item"
              @click="filterByScore(item.score)"
            >
              <div class="dist-score">
                <span>{{ item.score }}</span>
                <el-icon><Star /></el-icon>
              </div>
              <el-progress
                :percentage="item.percent"
                :stroke-width="12"
                :show-text="false"
                status="success"
                class="dist-progress"
              />
              <div class="dist-count">
                {{ item.count }}条 ({{ item.percent }}%)
              </div>
            </div>
          </div>
        </el-card>
      </el-col>
      <el-col :span="16">
        <el-card shadow="never" class="filter-card">
          <el-form :inline="true" :model="filterForm" class="filter-form">
            <el-form-item label="评分">
              <el-select
                v-model="filterForm.scoreFilter"
                placeholder="全部"
                clearable
                style="width: 120px"
                @change="handleFilterChange"
              >
                <el-option label="5星" :value="5" />
                <el-option label="4星" :value="4" />
                <el-option label="3星" :value="3" />
                <el-option label="2星" :value="2" />
                <el-option label="1星" :value="1" />
              </el-select>
            </el-form-item>
            <el-form-item label="房型">
              <el-select
                v-model="filterForm.roomTypeId"
                placeholder="全部房型"
                clearable
                style="width: 160px"
                @change="handleFilterChange"
              >
                <el-option
                  v-for="rt in roomTypeList"
                  :key="rt.id"
                  :label="rt.roomTypeName"
                  :value="rt.id"
                />
              </el-select>
            </el-form-item>
            <el-form-item label="图片">
              <el-select
                v-model="filterForm.hasImage"
                placeholder="全部"
                clearable
                style="width: 120px"
                @change="handleFilterChange"
              >
                <el-option label="有图片" :value="true" />
              </el-select>
            </el-form-item>
            <el-form-item label="排序">
              <el-select
                v-model="filterForm.sortType"
                style="width: 160px"
                @change="handleFilterChange"
              >
                <el-option label="最新评价" value="time_desc" />
                <el-option label="评分从高到低" value="score_desc" />
                <el-option label="评分从低到高" value="score_asc" />
              </el-select>
            </el-form-item>
          </el-form>
        </el-card>
      </el-col>
    </el-row>

    <el-card shadow="never" class="list-card">
      <div v-loading="listLoading" class="review-card-list">
        <template v-if="reviewList && reviewList.length > 0">
          <div
            v-for="item in reviewList"
            :key="item.review.id"
            class="review-card"
          >
            <div class="card-header">
              <div class="user-info">
                <el-avatar :size="40" class="user-avatar">
                  {{ getInitial(item) }}
                </el-avatar>
                <div class="user-detail">
                  <div class="user-name">
                    <span class="name-text">
                      {{ item.review.isAnonymous === 1 ? '匿名用户' : item.review.customerName }}
                    </span>
                    <el-tag
                      v-if="item.member"
                      size="small"
                      type="warning"
                      effect="light"
                      class="member-tag"
                    >
                      {{ item.member.levelName }}
                    </el-tag>
                  </div>
                  <div class="user-meta">
                    <span>{{ item.review.reviewTime?.split(' ')[0] }}</span>
                    <span class="divider">·</span>
                    <span>{{ item.review.roomTypeName }}</span>
                  </div>
                </div>
              </div>
              <div class="score-info">
                <el-rate
                  :model-value="Number(item.review.overallScore)"
                  disabled
                  size="small"
                  :max="5"
                />
                <span class="score-num">{{ item.review.overallScore }}</span>
              </div>
            </div>

            <div v-if="item.tags && item.tags.length > 0" class="card-tags">
              <el-tag
                v-for="tag in item.tags"
                :key="tag.id"
                :type="tag.tagType === 1 ? 'success' : 'danger'"
                effect="plain"
                size="small"
                class="review-tag"
              >
                {{ tag.tagText }}
              </el-tag>
            </div>

            <div v-if="item.review.reviewContent" class="card-content">
              {{ item.review.reviewContent }}
            </div>

            <div
              v-if="item.review.images && item.review.images.length > 0"
              class="card-images"
            >
              <el-image
                v-for="(img, idx) in item.review.images"
                :key="img.id"
                :src="img.imageUrl"
                :preview-src-list="getImageList(item)"
                :initial-index="idx"
                fit="cover"
                class="card-image"
                preview-teleported
              />
            </div>

            <div v-if="item.review.replyContent" class="card-reply">
              <div class="reply-icon">
                <el-icon><ChatLineSquare /></el-icon>
              </div>
              <div class="reply-body">
                <div class="reply-title">
                  <strong>酒店回复</strong>
                  <span class="reply-time">{{ item.review.replyTime?.split(' ')[0] }}</span>
                </div>
                <div class="reply-text">{{ item.review.replyContent }}</div>
              </div>
            </div>
          </div>
        </template>
        <el-empty v-else description="暂无评价数据" />
      </div>

      <el-pagination
        v-model:current-page="pagination.pageNum"
        v-model:page-size="pagination.pageSize"
        :page-sizes="[10, 15, 20]"
        :total="pagination.total"
        layout="total, sizes, prev, pager, next, jumper"
        background
        @size-change="fetchList"
        @current-change="fetchList"
        class="pagination"
      />
    </el-card>
  </div>
</template>

<script setup>
import { ref, reactive, onMounted } from 'vue'
import { ElMessage } from 'element-plus'
import { ChatDotRound, Star, CircleCheckFilled, ChatLineSquare } from '@element-plus/icons-vue'
import api from '@/api'

const statistics = ref({
  totalCount: 0,
  avgScore: '0.0',
  goodRate: 0,
  scoreDistribution: []
})
const roomTypeList = ref([])

const filterForm = reactive({
  scoreFilter: null,
  roomTypeId: null,
  hasImage: null,
  sortType: 'time_desc'
})

const pagination = reactive({
  pageNum: 1,
  pageSize: 10,
  total: 0
})

const listLoading = ref(false)
const reviewList = ref([])

const getInitial = (item) => {
  if (item.review.isAnonymous === 1) return '匿'
  const name = item.review.customerName || '用'
  return name.charAt(0)
}

const getImageList = (item) => {
  if (!item.review.images) return []
  return item.review.images.map(img => img.imageUrl)
}

const filterByScore = (score) => {
  filterForm.scoreFilter = score
  pagination.pageNum = 1
  fetchList()
}

const handleFilterChange = () => {
  pagination.pageNum = 1
  fetchList()
}

const fetchStatistics = async () => {
  try {
    const res = await api.reviewDisplay.getStatistics()
    statistics.value = res.data || {}
  } catch (e) {
    console.error(e)
  }
}

const fetchRoomTypes = async () => {
  try {
    const res = await api.reviewDisplay.getRoomTypes()
    roomTypeList.value = res.data || []
  } catch (e) {
    console.error(e)
  }
}

const fetchList = async () => {
  listLoading.value = true
  try {
    const params = {
      sortType: filterForm.sortType,
      pageNum: pagination.pageNum,
      pageSize: pagination.pageSize
    }
    if (filterForm.scoreFilter) params.scoreFilter = filterForm.scoreFilter
    if (filterForm.roomTypeId) params.roomTypeId = filterForm.roomTypeId
    if (filterForm.hasImage !== null) params.hasImage = filterForm.hasImage

    const res = await api.reviewDisplay.getPage(params)
    reviewList.value = res.data.list || []
    pagination.total = res.data.total || 0
  } catch (e) {
    console.error(e)
  } finally {
    listLoading.value = false
  }
}

onMounted(() => {
  fetchStatistics()
  fetchRoomTypes()
  fetchList()
})
</script>

<style lang="scss" scoped>
.review-display-container {
  .stats-card {
    margin-bottom: 20px;

    .stat-item {
      display: flex;
      align-items: center;
      gap: 16px;
      padding: 8px 0;

      .stat-icon {
        width: 56px;
        height: 56px;
        border-radius: 12px;
        display: flex;
        align-items: center;
        justify-content: center;
        font-size: 28px;
        color: #fff;
      }

      .stat-info {
        flex: 1;
        display: flex;
        align-items: baseline;
        flex-wrap: wrap;
        gap: 4px;
      }

      .stat-label {
        width: 100%;
        font-size: 13px;
        color: #6b7280;
        margin-bottom: 4px;
      }

      .stat-value {
        font-size: 28px;
        font-weight: bold;
        line-height: 1;
      }

      .stat-unit {
        font-size: 14px;
        color: #6b7280;
      }

      &.total-count {
        .stat-icon {
          background: linear-gradient(135deg, #60a5fa, #3b82f6);
        }
        .stat-value {
          color: #2563eb;
        }
      }

      &.avg-score {
        .stat-icon {
          background: linear-gradient(135deg, #fbbf24, #f59e0b);
        }
        .stat-value {
          color: #d97706;
        }
      }

      &.good-rate {
        .stat-icon {
          background: linear-gradient(135deg, #34d399, #10b981);
        }
        .stat-value {
          color: #059669;
        }
      }
    }
  }

  .distribution-card {
    margin-bottom: 20px;

    .card-title {
      font-size: 16px;
      font-weight: 600;
      margin-bottom: 20px;
      color: #1f2937;
    }

    .distribution-list {
      .distribution-item {
        display: flex;
        align-items: center;
        gap: 12px;
        padding: 10px 0;
        cursor: pointer;
        border-radius: 6px;
        padding-left: 8px;
        padding-right: 8px;
        transition: background 0.2s;

        &:hover {
          background: #f9fafb;
        }

        .dist-score {
          width: 54px;
          font-size: 14px;
          font-weight: 600;
          color: #d97706;
          display: flex;
          align-items: center;
          gap: 2px;

          .el-icon {
            font-size: 14px;
            color: #fbbf24;
          }
        }

        .dist-progress {
          flex: 1;
          max-width: 180px;
        }

        .dist-count {
          width: 100px;
          font-size: 12px;
          color: #6b7280;
          text-align: right;
        }
      }
    }
  }

  .filter-card {
    margin-bottom: 20px;

    .filter-form {
      margin: 0;
    }
  }

  .list-card {
    .review-card-list {
      .review-card {
        padding: 20px 0;
        border-bottom: 1px solid #f3f4f6;

        &:last-child {
          border-bottom: none;
        }

        .card-header {
          display: flex;
          justify-content: space-between;
          align-items: flex-start;
          margin-bottom: 14px;

          .user-info {
            display: flex;
            gap: 12px;
            align-items: center;

            .user-avatar {
              background: linear-gradient(135deg, #60a5fa, #3b82f6);
            }

            .user-detail {
              .user-name {
                display: flex;
                align-items: center;
                gap: 8px;
                margin-bottom: 4px;

                .name-text {
                  font-size: 15px;
                  font-weight: 600;
                  color: #1f2937;
                }
              }

              .user-meta {
                font-size: 13px;
                color: #6b7280;
                display: flex;
                align-items: center;
                gap: 6px;

                .divider {
                  color: #d1d5db;
                }
              }
            }
          }

          .score-info {
            display: flex;
            align-items: center;
            gap: 8px;

            .score-num {
              font-size: 18px;
              font-weight: bold;
              color: #f59e0b;
            }
          }
        }

        .card-tags {
          margin-bottom: 12px;

          .review-tag {
            margin-right: 8px;
            margin-bottom: 6px;
          }
        }

        .card-content {
          font-size: 14px;
          line-height: 1.75;
          color: #374151;
          margin-bottom: 12px;
          white-space: pre-wrap;
        }

        .card-images {
          display: flex;
          gap: 10px;
          flex-wrap: wrap;
          margin-bottom: 16px;

          .card-image {
            width: 110px;
            height: 110px;
            border-radius: 8px;
            border: 1px solid #e5e7eb;
          }
        }

        .card-reply {
          display: flex;
          gap: 12px;
          padding: 14px 16px;
          background: #eff6ff;
          border-radius: 8px;
          border-left: 4px solid #3b82f6;

          .reply-icon {
            color: #3b82f6;
            font-size: 20px;
            margin-top: 2px;
          }

          .reply-body {
            flex: 1;

            .reply-title {
              margin-bottom: 6px;
              font-size: 13px;
              display: flex;
              align-items: center;
              gap: 10px;
              color: #1e40af;

              .reply-time {
                font-size: 12px;
                font-weight: normal;
                color: #6b7280;
              }
            }

            .reply-text {
              font-size: 14px;
              line-height: 1.7;
              color: #1f2937;
              white-space: pre-wrap;
            }
          }
        }
      }
    }

    .pagination {
      margin-top: 20px;
      display: flex;
      justify-content: center;
    }
  }
}
</style>
