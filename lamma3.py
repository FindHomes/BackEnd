# 필요한 라이브러리 설치
!pip install transformers datasets torch

# 라이브러리 임포트
from transformers import LlamaForSequenceClassification, Trainer, TrainingArguments
from datasets import load_dataset

# 데이터셋 로드 및 전처리
dataset = load_dataset('your_dataset_name')

def preprocess_function(examples):
    return tokenizer(examples['text'], truncation=True, padding='max_length')

encoded_dataset = dataset.map(preprocess_function, batched=True)

# 모델과 토크나이저 로드
model = LlamaForSequenceClassification.from_pretrained('llama3-base')
tokenizer = LlamaTokenizer.from_pretrained('llama3-base')

# 트레이닝 설정
training_args = TrainingArguments(
    output_dir='./results',          # 출력 디렉토리
    evaluation_strategy='epoch',     # 평가 전략
    per_device_train_batch_size=8,   # 배치 사이즈
    per_device_eval_batch_size=8,    # 평가 배치 사이즈
    num_train_epochs=3,              # 에포크 수
    weight_decay=0.01,               # 가중치 감소
)

# Trainer 생성
trainer = Trainer(
    model=model,
    args=training_args,
    train_dataset=encoded_dataset['train'],
    eval_dataset=encoded_dataset['test'],
)

# 파인튜닝 수행
trainer.train()

# 모델 저장
model.save_pretrained('./fine_tuned_llama3')
tokenizer.save_pretrained('./fine_tuned_llama3')
