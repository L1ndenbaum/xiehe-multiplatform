
export const annotationData = {
  imageId: 'IMG001',
  patientId: 'P20240115001',
  patientName: '王**',
  fileName: '20240115_spine_lateral.png',
  checkType: 'XR',
  measurements: [
    { id: 'M001', type: 'T1倾斜', value: '-8.5°', color: '#FF6B6B' },
    { id: 'M002', type: '斜肩', value: '辅助标记', color: '#4ECDC4' },
    { id: 'M003', type: 'RSH', value: '辅助标记', color: '#45B7D1' },
    { id: 'M004', type: '骨盆', value: '0.4°', color: '#96CEB4' },
    { id: 'M005', type: '骶骨', value: '1.3°', color: '#FFEAA7' },
    { id: 'M006', type: '日均矫正角度', value: '12.9毫米', color: '#DDA0DD' },
    { id: 'M007', type: 'TS', value: '11.4毫米', color: '#98D8C8' }
  ],
  cobbAngles: [
    { id: 'C001', name: 'TS', value: '11.4mm', position: 'top' },
    { id: 'C002', name: 'RSH', value: '辅助标记', position: 'top-middle' },
    { id: 'C003', name: 'AVT', value: '12.9mm', position: 'middle' },
    { id: 'C004', name: 'Cobb', value: '辅助标记', position: 'middle' },
    { id: 'C005', name: 'Pelvic', value: '0.4°', position: 'bottom' },
    { id: 'C006', name: 'Sacral', value: '1.3°', position: 'bottom' }
  ],
  tools: [
    { id: 'T001', name: 'T1倾斜', icon: 'ri-focus-line' },
    { id: 'T002', name: '斜肩测量', icon: 'ri-ruler-line' },
    { id: 'T003', name: 'Cobb+Lumbar', icon: 'ri-compass-3-line' },
    { id: 'T004', name: '10M', icon: 'ri-pencil-ruler-line' },
    { id: 'T005', name: '骨盆', icon: 'ri-shape-line' },
    { id: 'T006', name: '躯干', icon: 'ri-body-scan-line' },
    { id: 'T007', name: 'E小椎体定点', icon: 'ri-focus-2-line' },
    { id: 'T008', name: 'TS', icon: 'ri-crosshair-line' }
  ],
  shapes: [
    { id: 'S001', name: '矩形', icon: 'ri-checkbox-blank-line' },
    { id: 'S002', name: '椭圆', icon: 'ri-checkbox-blank-circle-line' },
    { id: 'S003', name: '盒子', icon: 'ri-box-3-line' },
    { id: 'S004', name: '穿', icon: 'ri-arrow-right-line' },
    { id: 'S005', name: '区域', icon: 'ri-crop-line' }
  ],
  imageSettings: {
    contrast: 100,
    brightness: 0,
    hue: 0,
    standardDistance: 100
  }
};

export const annotationHistory = [
  {
    id: 'AH001',
    imageId: 'IMG001',
    operator: '张医生',
    action: '添加Cobb角测量',
    time: '2024/01/15 15:30',
    result: '侧弯角度 12.9°'
  },
  {
    id: 'AH002',
    imageId: 'IMG001',
    operator: '张医生',
    action: '添加骨盆倾斜测量',
    time: '2024/01/15 15:25',
    result: '倾斜角度 0.4°'
  },
  {
    id: 'AH003',
    imageId: 'IMG001',
    operator: 'AI辅助',
    action: '自动识别脊柱轮廓',
    time: '2024/01/15 15:20',
    result: '识别完成'
  }
];
