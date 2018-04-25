FROM python:3.6.5-alpine

WORKDIR /usr/src/app

# Copy requirements in its own layer
COPY requirements.txt ./
RUN pip install --no-cache-dir -r requirements.txt

# Copy the rest of the code
COPY . .

ENV BACKEND "vote-backend:8080"

EXPOSE 8080
CMD [ "python", "./app.py" ]

#ENTRYPOINT ["python"]
#CMD ["app.py"]
